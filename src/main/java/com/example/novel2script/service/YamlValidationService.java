package com.example.novel2script.service;

import com.example.novel2script.dto.ValidateYamlResponse;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class YamlValidationService {

    private final Yaml yaml = new Yaml();

    public ValidateYamlResponse validate(String yamlContent) {
        ValidateYamlResponse response = new ValidateYamlResponse();
        response.setValid(true);
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (yamlContent == null || yamlContent.isBlank()) {
            errors.add("YAML content is empty");
            response.setValid(false);
            response.setErrors(errors);
            return response;
        }

        try {
            Object parsed = yaml.load(yamlContent);
            if (parsed == null) {
                errors.add("YAML parsed to null");
                response.setValid(false);
                response.setErrors(errors);
                return response;
            }

            if (!(parsed instanceof Map)) {
                errors.add("YAML root must be a map/object");
                response.setValid(false);
                response.setErrors(errors);
                return response;
            }

            Map<?, ?> rootMap = (Map<?, ?>) parsed;
            validateStructure(rootMap, errors, warnings);

            response.setValid(errors.isEmpty());
            response.setErrors(errors);
            response.setWarnings(warnings);

        } catch (ConstructorException e) {
            errors.add("YAML syntax error: " + e.getMessage());
            response.setValid(false);
            response.setErrors(errors);
        } catch (Exception e) {
            errors.add("Failed to parse YAML: " + e.getMessage());
            response.setValid(false);
            response.setErrors(errors);
        }

        return response;
    }

    private void validateStructure(Map<?, ?> rootMap, List<String> errors, List<String> warnings) {
        if (!rootMap.containsKey("script")) {
            errors.add("Missing required root key 'script'");
            return;
        }

        Object scriptObj = rootMap.get("script");
        if (!(scriptObj instanceof Map)) {
            errors.add("'script' must be a map/object");
            return;
        }

        Map<?, ?> scriptMap = (Map<?, ?>) scriptObj;

        checkRequired(scriptMap, "schema_version", errors);
        checkRequired(scriptMap, "title", errors);
        checkRequired(scriptMap, "metadata", errors);
        checkRequired(scriptMap, "source", errors);
        checkRequired(scriptMap, "characters", errors, true);
        checkRequired(scriptMap, "scenes", errors, true);

        Object charactersObj = scriptMap.get("characters");
        if (charactersObj instanceof List) {
            List<?> characters = (List<?>) charactersObj;
            if (characters.isEmpty()) {
                errors.add("'characters' array must not be empty");
            }
            for (int i = 0; i < characters.size(); i++) {
                Object charObj = characters.get(i);
                if (charObj instanceof Map) {
                    Map<?, ?> charMap = (Map<?, ?>) charObj;
                    if (!charMap.containsKey("id")) {
                        errors.add("Character at index " + i + " missing 'id'");
                    }
                    if (!charMap.containsKey("name")) {
                        errors.add("Character at index " + i + " missing 'name'");
                    }
                }
            }
        }

        Object scenesObj = scriptMap.get("scenes");
        if (scenesObj instanceof List) {
            List<?> scenes = (List<?>) scenesObj;
            if (scenes.isEmpty()) {
                errors.add("'scenes' array must not be empty");
            }
            for (int i = 0; i < scenes.size(); i++) {
                Object sceneObj = scenes.get(i);
                if (sceneObj instanceof Map) {
                    Map<?, ?> sceneMap = (Map<?, ?>) sceneObj;
                    if (!sceneMap.containsKey("id")) {
                        errors.add("Scene at index " + i + " missing 'id'");
                    }
                    if (!sceneMap.containsKey("title")) {
                        errors.add("Scene at index " + i + " missing 'title'");
                    }
                    if (!sceneMap.containsKey("beats")) {
                        errors.add("Scene at index " + i + " missing 'beats'");
                    } else {
                        Object beatsObj = sceneMap.get("beats");
                        if (beatsObj instanceof List) {
                            List<?> beats = (List<?>) beatsObj;
                            if (beats.isEmpty()) {
                                errors.add("Scene at index " + i + " has empty 'beats' array");
                            }
                            for (int j = 0; j < beats.size(); j++) {
                                Object beatObj = beats.get(j);
                                if (beatObj instanceof Map) {
                                    Map<?, ?> beatMap = (Map<?, ?>) beatObj;
                                    String type = String.valueOf(beatMap.get("type"));
                                    if ("dialogue".equals(type)) {
                                        if (!beatMap.containsKey("character_id")) {
                                            errors.add("Dialogue beat at scene " + i + ", beat " + j + " missing 'character_id'");
                                        }
                                        if (!beatMap.containsKey("character_name")) {
                                            errors.add("Dialogue beat at scene " + i + ", beat " + j + " missing 'character_name'");
                                        }
                                        if (!beatMap.containsKey("content")) {
                                            errors.add("Dialogue beat at scene " + i + ", beat " + j + " missing 'content'");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkRequired(Map<?, ?> map, String key, List<String> errors) {
        checkRequired(map, key, errors, false);
    }

    private void checkRequired(Map<?, ?> map, String key, List<String> errors, boolean isList) {
        if (!map.containsKey(key)) {
            errors.add("Missing required field 'script." + key + "'");
        } else if (isList) {
            Object value = map.get(key);
            if (!(value instanceof List)) {
                errors.add("'script." + key + "' must be an array");
            }
        } else {
            Object value = map.get(key);
            if (value == null || (value instanceof String && ((String) value).isBlank())) {
                errors.add("'script." + key + "' must not be empty");
            }
        }
    }

    public String extractScriptContent(String yamlContent) {
        try {
            Object parsed = yaml.load(yamlContent);
            if (parsed instanceof Map) {
                Map<?, ?> rootMap = (Map<?, ?>) parsed;
                if (rootMap.containsKey("script")) {
                    Object scriptObj = rootMap.get("script");
                    if (scriptObj instanceof Map) {
                        Map<?, ?> scriptMap = (Map<?, ?>) scriptObj;
                        Object title = scriptMap.get("title");
                        if (title != null) {
                            return title.toString();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "Unknown";
    }
}
