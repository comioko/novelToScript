package com.example.novel2script.service;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    private static final String GENERATION_SYSTEM_PROMPT = """
你是一位专业的剧本改编专家。请将小说文本改编为结构化剧本 YAML。

【重要规则】
1. 只输出 YAML，不要输出任何解释文字
2. YAML 必须符合以下 Schema 结构
3. 内容必须来自输入小说，不得凭空编造
4. 对白要自然流畅，符合角色性格
5. 动作描写简洁，可拍摄
6. 心理描写应转化为动作、对白或旁白
7. 如果信息不足，可以合理补全，但不要偏离原文核心

【YAML Schema】
```yaml
script:
  schema_version: "1.0"
  title: "剧本标题"
  logline: "一句话故事梗概（不超过50字）"
  synopsis: "完整故事梗概"
  metadata:
    script_type: "SCREENPLAY|DRAMATIC|SHORT_DRAMA|RADIO_DRAMA"
    target_language: "zh-CN|en-US"
    adaptation_style: "FAITHFUL|DRAMATIC|COMMERCIAL|SUSPENSEFUL|COMEDIC"
    target_duration_minutes: 数字
    generated_at: "ISO时间格式"
  source:
    type: "novel"
    chapter_count: 数字
    chapters:
      - id: "chapter_001"
        index: 1
        title: "章节标题"
        summary: "章节摘要"
  characters:
    - id: "char_001"
      name: "角色名"
      role: "protagonist|antagonist|supporting"
      description: "角色描述"
      motivation: "角色动机"
      relationships:
        - target_character_id: "char_002"
          relation: "关系描述"
  scenes:
    - id: "scene_001"
      title: "场景标题"
      source_chapters: ["chapter_001"]
      location: "地点"
      time: "时间"
      characters: ["char_001"]
      summary: "场景摘要"
      dramatic_function: "该场景在剧作结构中的作用"
      beats:
        - type: "action|dialogue|narration|transition|sound|camera"
          content: "内容"
          character_id: "char_001"  # dialogue 时必须有
          character_name: "角色名"   # dialogue 时必须有
  notes:
    adaptation_notes: []
    potential_improvements: []
```

【Beat 类型说明】
- action: 动作/场景描写
- dialogue: 对白（必须包含 character_id 和 character_name）
- narration: 旁白/叙述
- transition: 转场
- sound: 音效
- camera: 镜头指示

请严格按照上述 Schema 输出 YAML。
""";

    private static final String REPAIR_SYSTEM_PROMPT = """
你是一位 YAML 修复专家。以下 YAML 可能存在语法错误或缺少必要字段。

【你的任务】
1. 修复 YAML 语法错误
2. 补全缺失的必需字段
3. 不要改变核心剧情内容
4. 只输出修复后的完整 YAML

【必须保留的字段】
- script.schema_version
- script.title
- script.characters (至少1个)
- script.scenes (至少1个)
- script.scenes[].beats (每个场景至少1个 beat)

【输出格式】
只输出 YAML，不要任何解释。
""";

    public String buildGenerationPrompt(String novelText, String scriptType, String targetLanguage,
                                        String adaptationStyle, int targetDurationMinutes,
                                        String title, String chaptersSummary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("【小说文本】\n\n");
        prompt.append(novelText);
        prompt.append("\n\n【改编要求】\n");
        prompt.append("- 剧本类型: ").append(scriptType).append("\n");
        prompt.append("- 目标语言: ").append(targetLanguage).append("\n");
        prompt.append("- 改编风格: ").append(adaptationStyle).append("\n");
        prompt.append("- 目标时长: ").append(targetDurationMinutes).append(" 分钟\n");
        prompt.append("- 小说标题: ").append(title).append("\n");
        prompt.append("- 章节信息: ").append(chaptersSummary).append("\n");
        prompt.append("\n请将上述小说文本改编为 YAML 格式的剧本。");
        return prompt.toString();
    }

    public String buildRepairPrompt(String brokenYaml) {
        return "【有问题的 YAML】\n\n" + brokenYaml + "\n\n请修复上述 YAML 的错误并输出正确的完整 YAML。";
    }

    public String getGenerationSystemPrompt() {
        return GENERATION_SYSTEM_PROMPT;
    }

    public String getRepairSystemPrompt() {
        return REPAIR_SYSTEM_PROMPT;
    }
}
