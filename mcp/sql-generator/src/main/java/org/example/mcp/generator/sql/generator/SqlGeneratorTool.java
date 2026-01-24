package org.example.mcp.generator.sql.generator;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.chat.client.ChatClient;
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

@Slf4j
@Component
public class SqlGeneratorTool {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatClient chatClient;

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

        return chatClient.prompt()
                .system("""
                        Ты - SQL консультант с доступом к базе знаний о структуре базы данных.
                        Твоя задача - генерировать SQL запросы на основе предоставленного контекста.
                        Ты можешь генерировать запросы только с теми именами колонок, таблиц и схем которые я передаю в контексте. 
                        
                        Инструкции:
                        1. Анализируй контекст, чтобы понять структуру базы данных
                        2. Генерируй ТОЛЬКО SQL запрос без пояснений
                        3. Используй правильные имена таблиц и полей из контекста
                        4. Все таблицы находятся в схеме "datamart"
                        5. Если запрос сложный, используй JOIN и WHERE правильно
                        6. Если информации недостаточно, верни "Для правильного доступа к данным не достаточно информации"
                        7. Все таблицы и колонки содержатся в контексте который я предоставляю
                        8. Не используй никакую дополнительную информацию кроме той которую я передаю в контексте
                        9. Все таблицы которые ты используешь в итоговом sql запросе, находятся в контексте который я передаю
                        Контекст базы данных:
                        {context}
                        
                        Правила:
                        - Всегда используй полные имена таблиц: схемма.таблица
                        - Для поиска по ID используй WHERE
                        """)
                .user("Сгенерируй SQL запрос для: " + question + "\n\nВерни ТОЛЬКО SQL запрос без дополнительного текста.")
                .advisors(advisor -> advisor.param("context", context))
                .call()
                .content();
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
