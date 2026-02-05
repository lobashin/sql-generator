package org.example.mcp.sql.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DatabaseMcpTools {

    public static final Logger log = LoggerFactory.getLogger(DatabaseMcpTools.class);

    private final VectorStore vectorStore;

    public DatabaseMcpTools(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @McpTool(
            name = "structure_database",
            description = "Схема базы данных"
    )
    public String structureDatabase(@McpToolParam(description = "Поисковый запрос") String question) {
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(1)
                        .similarityThreshold(0.80)
                        .build()
        );

        assert relevantDocs != null;
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info(">> Описание конктретной схемы базы данных: {}", question);
        log.info(">> Найдено {} релевантных документов", relevantDocs.size());
        log.info(">> Контекст: {}", context);

        return context;
    }

    @McpTool(
            name = "table_database",
            description = "Структура таблицы"
    )
    public String tableDatabase(
            @McpToolParam(description = "Поисковый запрос") String question) {

        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(1)
                        .similarityThreshold(0.85)
                        .build()
        );

        assert relevantDocs != null;
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info(">> Описание конктертной таблицы: {}", question);
        log.info(">> Найдено {} релевантных документов", relevantDocs.size());
        log.info(">> Контекст: {}", context);

        return context;
    }

}
