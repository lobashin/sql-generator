package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dto.MessageRequest;
import org.example.service.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "RAG API", description = "API для генерации SQL запросов")
public class RagController {

    private final RagService ragService;

    @Operation(
            summary = "Генерация SQL запроса",
            description = "Принимает естественный язык и возвращает SQL запрос"
    )
    @PostMapping("/ai/rag")
    public String generate(@RequestBody MessageRequest request) {
        return ragService.retrieveAndGenerate(request.getMessage());
    }
}
