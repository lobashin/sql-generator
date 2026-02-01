package org.example.mcp.sql.generator;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SqlGeneratorTool {

    public static final Logger log = LoggerFactory.getLogger(SqlGeneratorTool.class);

    private final VectorStore vectorStore;

    public SqlGeneratorTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @McpTool(
            name = "query_knowledge_base",
            description = "Ищет информацию в базе знаний и генерирует ответ с использованием RAG"
    )
    public String queryKnowledgeBase(
            @McpToolParam(description = "Вопрос для поиска") String question,
            @McpToolParam(description = "Количество релевантных документов", required = false) Integer topK) {
        int k = (topK != null) ? topK : 6;

        // Поиск релевантных документов
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(k)
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

    @McpTool(
            name = "add_to_knowledge_base",
            description = "Добавляет новые документы в базу знаний"
    )
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

    @McpTool(
            name = "search_similar",
            description = "Ищет семантически похожие документы"
    )
    public List<Map<String, Object>> searchSimilarDocuments(
            @McpToolParam(description = "Запрос для поиска") String query,
            @McpToolParam(description = "Порог схожести", required = false) Double threshold) {
        double similarityThreshold = (threshold != null) ? threshold : 0.7;

        List<org.springframework.ai.document.Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(query)
                        .topK(10)
                        .similarityThreshold((similarityThreshold)).build());

        assert results != null;
        return results.stream()
                .map(doc -> Map.of(
                        "content", Objects.requireNonNull(doc.getText()),
                        "metadata", doc.getMetadata(),
                        "score", doc.getMetadata().get("similarity")
                ))
                .collect(Collectors.toList());
    }

}
