package org.example.mcp.sql.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SqlGeneratorTool {

    public static final Logger log = LoggerFactory.getLogger(SqlGeneratorTool.class);

    private final VectorStore vectorStore;

    public SqlGeneratorTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @McpTool(
            name = "structure_database",
            description = "Получить описание структуры базы данных"
    )
    public String structureDatabase() {
        var question = "Структура базы";
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(1)
                        .similarityThreshold(0.65)
                        .build()
        );

        assert relevantDocs != null;
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info(">> поисковый запрос: {}", question);
        log.info(">> Найдено {} релевантных документов", relevantDocs.size());
        log.info(">> Контекст: {}", context);

        return context;
    }

    @McpTool(
            name = "table_database",
            description = "Описание конктертной таблицы"
    )
    public String tableDatabase(
            @McpToolParam(description = "Наименование на русском") String question) {

        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(1)
                        .similarityThreshold(0.65)
                        .build()
        );

        assert relevantDocs != null;
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info(">> Запрос пользователя: {}", question);
        log.info(">> Найдено {} релевантных документов", relevantDocs.size());
        log.info(">> Контекст: {}", context);

        return context;
    }

//    @McpTool(
//            name = "add_to_knowledge_base",
//            description = "Добавляет новые документы в базу знаний"
//    )
    public String addToKnowledgeBase(
            @McpToolParam(description = "Список документов") List<Map<String, Object>> documents) {

        if (documents == null || documents.isEmpty()) {
            return "Error: Documents list is empty";
        }

        List<Document> aiDocuments = new ArrayList<>();

        for (Map<String, Object> doc : documents) {
            String content = (String) doc.get("content");
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) doc.get("metadata");

            if (content == null || content.trim().isEmpty()) {
                continue;
            }

            Document aiDocument =
                    new Document(content, metadata);
            aiDocuments.add(aiDocument);
        }

        if (!aiDocuments.isEmpty()) {
            vectorStore.add(aiDocuments);
            return String.format("Успешно добавлено %d документов в базу знаний", aiDocuments.size());
        } else {
            return "Error: No valid documents to add";
        }
    }

}
