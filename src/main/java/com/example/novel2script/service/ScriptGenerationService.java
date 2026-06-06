package com.example.novel2script.service;

import com.example.novel2script.dto.*;
import com.example.novel2script.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScriptGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ScriptGenerationService.class);

    private final NovelParserService novelParserService;
    private final PromptBuilderService promptBuilderService;
    private final AiClient aiClient;
    private final YamlValidationService yamlValidationService;

    public ScriptGenerationService(NovelParserService novelParserService,
                                   PromptBuilderService promptBuilderService,
                                   AiClient aiClient,
                                   YamlValidationService yamlValidationService) {
        this.novelParserService = novelParserService;
        this.promptBuilderService = promptBuilderService;
        this.aiClient = aiClient;
        this.yamlValidationService = yamlValidationService;
    }

    public GenerateScriptResponse generateScript(GenerateScriptRequest request) {
        GenerateScriptResponse response = new GenerateScriptResponse();
        response.setSuccess(false);

        String novelText = request.getNovelText();
        if (novelText == null || novelText.isBlank()) {
            response.setMessage("小说文本不能为空");
            return response;
        }

        List<ChapterDto> chapters = novelParserService.parseChapters(novelText);
        if (chapters.size() < 3) {
            response.setMessage("检测到 " + chapters.size() + " 个章节，但至少需要 3 个章节才能生成剧本");
            response.setChapters(chapters);
            return response;
        }

        response.setChapters(chapters);

        String title = novelParserService.extractTitle(novelText);
        String chaptersSummary = chapters.stream()
                .map(c -> c.getTitle() + ": " + (c.getContentPreview() != null ?
                        c.getContentPreview().substring(0, Math.min(50, c.getContentPreview().length())) : ""))
                .collect(Collectors.joining("; "));

        String systemPrompt = promptBuilderService.getGenerationSystemPrompt();
        String userPrompt = promptBuilderService.buildGenerationPrompt(
                novelText,
                request.getScriptType(),
                request.getTargetLanguage(),
                request.getAdaptationStyle(),
                request.getTargetDurationMinutes(),
                title,
                chaptersSummary
        );

        boolean isMockMode = !aiClient.isConfigured();
        String yamlContent;
        try {
            yamlContent = aiClient.callChatCompletion(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("AI call failed", e);
            response.setMessage("AI 调用失败: " + e.getMessage());
            return response;
        }

        response.setMockMode(isMockMode);

        yamlContent = cleanYamlResponse(yamlContent);

        ValidateYamlResponse validation = yamlValidationService.validate(yamlContent);
        GenerateScriptResponse.ValidationResult validationResult = new GenerateScriptResponse.ValidationResult();
        validationResult.setValid(validation.isValid());
        validationResult.setErrors(validation.getErrors());
        validationResult.setWarnings(validation.getWarnings());
        response.setValidation(validationResult);

        if (!validation.isValid() && shouldAttemptRepair(validation)) {
            yamlContent = attemptRepair(yamlContent);
            validation = yamlValidationService.validate(yamlContent);
            validationResult.setValid(validation.isValid());
            validationResult.setErrors(validation.getErrors());
            validationResult.setWarnings(validation.getWarnings());
            response.setValidation(validationResult);
        }

        response.setYaml(yamlContent);
        response.setSuccess(true);
        response.setMessage("剧本生成" + (validation.isValid() ? "成功" : "成功但存在警告"));

        return response;
    }

    private String cleanYamlResponse(String content) {
        if (content == null) {
            return "";
        }

        String cleaned = content.trim();

        int yamlStart = cleaned.indexOf("script:");
        if (yamlStart > 0) {
            cleaned = cleaned.substring(yamlStart);
        }

        int firstBrace = cleaned.indexOf('{');
        int firstColon = cleaned.indexOf(':');
        if (firstBrace >= 0 && firstColon >= 0 && firstBrace < firstColon) {
            int lastBrace = cleaned.lastIndexOf('}');
            if (lastBrace > 0) {
                cleaned = cleaned.substring(0, lastBrace + 1);
            }
        }

        cleaned = cleaned.replaceAll("^```yaml\\s*", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");

        // Replace Chinese quotation marks with single quotes to prevent YAML parsing errors
        cleaned = cleaned.replace((char) 0x201C, '\'');  // left double quote
        cleaned = cleaned.replace((char) 0x201D, '\'');  // right double quote
        cleaned = cleaned.replace((char) 0x2018, '\'');  // left single quote
        cleaned = cleaned.replace((char) 0x2019, '\'');  // right single quote

        return cleaned.trim();
    }

    private boolean shouldAttemptRepair(ValidateYamlResponse validation) {
        List<String> errors = validation.getErrors();
        if (errors == null || errors.isEmpty()) {
            return false;
        }

        for (String error : errors) {
            if (error.contains("Missing required field") ||
                    error.contains("missing 'id'") ||
                    error.contains("missing 'title'") ||
                    error.contains("missing 'beats'") ||
                    error.contains("missing 'character_id'") ||
                    error.contains("missing 'character_name'") ||
                    error.contains("appears in dialogue but not in scene.characters")) {
                return true;
            }
        }
        return false;
    }

    private String attemptRepair(String brokenYaml) {
        try {
            String systemPrompt = promptBuilderService.getRepairSystemPrompt();
            String userPrompt = promptBuilderService.buildRepairPrompt(brokenYaml);
            String repaired = aiClient.callChatCompletion(systemPrompt, userPrompt);
            return cleanYamlResponse(repaired);
        } catch (Exception e) {
            log.warn("Repair attempt failed", e);
            return brokenYaml;
        }
    }

    public ValidateYamlResponse validateYaml(String yamlContent) {
        return yamlValidationService.validate(yamlContent);
    }
}
