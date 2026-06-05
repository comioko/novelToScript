package com.example.novel2script.dto;

public class GenerateScriptResponse {

    private boolean success;
    private String message;
    private boolean mockMode;
    private String yaml;
    private ValidationResult validation;
    private java.util.List<ChapterDto> chapters;

    public static class ValidationResult {
        private boolean valid;
        private java.util.List<String> errors;
        private java.util.List<String> warnings;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public java.util.List<String> getErrors() {
            return errors;
        }

        public void setErrors(java.util.List<String> errors) {
            this.errors = errors;
        }

        public java.util.List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(java.util.List<String> warnings) {
            this.warnings = warnings;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getYaml() {
        return yaml;
    }

    public void setYaml(String yaml) {
        this.yaml = yaml;
    }

    public ValidationResult getValidation() {
        return validation;
    }

    public void setValidation(ValidationResult validation) {
        this.validation = validation;
    }

    public java.util.List<ChapterDto> getChapters() {
        return chapters;
    }

    public void setChapters(java.util.List<ChapterDto> chapters) {
        this.chapters = chapters;
    }

    public boolean isMockMode() {
        return mockMode;
    }

    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }
}
