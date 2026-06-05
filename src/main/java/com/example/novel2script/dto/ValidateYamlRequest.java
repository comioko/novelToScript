package com.example.novel2script.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidateYamlRequest {

    @NotBlank(message = "YAML content cannot be empty")
    private String yaml;

    public String getYaml() {
        return yaml;
    }

    public void setYaml(String yaml) {
        this.yaml = yaml;
    }
}
