package org.example.mcp.generator.sql.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.mcp.generator.sql.SqlGeneratorTool;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "MCP API", description = "API для работы с базой знаний")
public class McpController {

    private final SqlGeneratorTool sqlGeneratorTool;

    @Operation(
            summary = "Запрос к базе знаний",
            description = "Ищет информацию в базе знаний через RAG"
    )
    @PostMapping("/ai/rag")
    public String generate(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        Integer topK = (Integer) request.get("topK");

        if (question == null || question.trim().isEmpty()) {
            return "Error: Question parameter is required";
        }

        try {
            return sqlGeneratorTool.queryKnowledgeBase(question, topK);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Operation(
            summary = "Добавить документ",
            description = "Добавляет документ в базу знаний"
    )
    @PostMapping("/ai/add-doc")
    public String addDocument(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");

        if (content == null || content.trim().isEmpty()) {
            return "Error: Content parameter is required";
        }

        try {
            return sqlGeneratorTool.addToKnowledgeBase(content, metadata);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Operation(
            summary = "Поиск похожих документов",
            description = "Находит семантически похожие документы"
    )
    @PostMapping("/ai/search-similar")
    public List<Map<String, Object>> searchSimilar(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        Double threshold = null;

        if (request.get("threshold") != null) {
            try {
                threshold = Double.parseDouble(request.get("threshold").toString());
            } catch (Exception ignored) {}
        }

        if (query == null || query.trim().isEmpty()) {
            return List.of(Map.of("error", "Query parameter is required"));
        }

        try {
            return sqlGeneratorTool.searchSimilarDocuments(query, threshold);
        } catch (Exception e) {
            return List.of(Map.of("error", e.getMessage()));
        }
    }
}