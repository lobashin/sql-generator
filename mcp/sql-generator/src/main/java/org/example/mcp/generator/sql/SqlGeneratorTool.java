package org.example.mcp.generator.sql;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        int k = (topK != null) ? topK : 5;

        // Поиск релевантных документов
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(k).build()
        );

        // Формирование контекста
        assert relevantDocs != null;
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // Генерация ответа с контекстом
        return chatClient.prompt()
                .system("""
                        Ты - помощник с доступом к базе знаний.
                        Отвечай ТОЛЬКО на основе предоставленного контекста.
                        Если ответа нет в контексте, скажи "Я не нашел информацию по этому вопросу в базе знаний".
                        
                        Контекст:
                        {context}
                        """)
                .user(question)
                .advisors(advisor -> advisor.param("context", context))
                .call()
                .content();
    }

    @McpTool(
            name = "add_to_knowledge_base",
            description = "Добавляет новый документ в базу знаний"
    )
    public String addToKnowledgeBase(
            @McpToolParam(description = "Текст документа") String content,
            @McpToolParam(description = "Метаданные документа", required = false) Map<String, Object> metadata) {
        org.springframework.ai.document.Document document =
                new org.springframework.ai.document.Document(content, metadata);

        vectorStore.add(List.of(document));

        return "Документ успешно добавлен в базу знаний";
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

        return results.stream()
                .map(doc -> Map.of(
                        "content", Objects.requireNonNull(doc.getText()),
                        "metadata", doc.getMetadata(),
                        "score", doc.getMetadata().get("similarity")
                ))
                .collect(Collectors.toList());
    }

}
