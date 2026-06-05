package com.example.novel2script.controller;

import com.example.novel2script.dto.*;
import com.example.novel2script.service.ScriptGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scripts")
public class ScriptController {

    private final ScriptGenerationService scriptGenerationService;

    public ScriptController(ScriptGenerationService scriptGenerationService) {
        this.scriptGenerationService = scriptGenerationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateScriptResponse> generateScript(@Valid @RequestBody GenerateScriptRequest request) {
        GenerateScriptResponse response = scriptGenerationService.generateScript(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateYamlResponse> validateYaml(@Valid @RequestBody ValidateYamlRequest request) {
        ValidateYamlResponse response = scriptGenerationService.validateYaml(request.getYaml());
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
