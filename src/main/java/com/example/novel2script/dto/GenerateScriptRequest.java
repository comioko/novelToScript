package com.example.novel2script.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class GenerateScriptRequest {

    @NotBlank(message = "Novel text cannot be empty")
    private String novelText;

    private String scriptType = "SCREENPLAY";
    private String targetLanguage = "zh-CN";
    private String adaptationStyle = "DRAMATIC";

    @Min(value = 1, message = "Target duration must be at least 1 minute")
    private int targetDurationMinutes = 30;

    public String getNovelText() {
        return novelText;
    }

    public void setNovelText(String novelText) {
        this.novelText = novelText;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getAdaptationStyle() {
        return adaptationStyle;
    }

    public void setAdaptationStyle(String adaptationStyle) {
        this.adaptationStyle = adaptationStyle;
    }

    public int getTargetDurationMinutes() {
        return targetDurationMinutes;
    }

    public void setTargetDurationMinutes(int targetDurationMinutes) {
        this.targetDurationMinutes = targetDurationMinutes;
    }
}
