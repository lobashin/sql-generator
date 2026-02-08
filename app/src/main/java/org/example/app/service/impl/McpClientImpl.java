package org.example.app.service.impl;

import org.example.app.service.McpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class McpClientImpl implements McpClient {

    private static final Logger log = LoggerFactory.getLogger(McpClientImpl.class);
    private final ChatClient chatClient;
    private final ToolCallbackProvider tools;
    private final VectorStore vectorStore;

    public McpClientImpl(ChatClient chatClient,
                         ToolCallbackProvider tools,
                         VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.tools = tools;
        this.vectorStore = vectorStore;
    }

    @Override
    public String requestResolveSql(String message) {
        return chatClient
                .prompt()
                .system("""
                        Ты - консультант, который генерирует SQL.
                        Сначала используй тул [Схема базы данных] для получения описания базы данных.
                        Затем примени тул [Структура таблицы] для получения данных о полях таблице.
                        Твой ответ будет использоваться для запроса на прямую из базы данных, поэтому отвечай ТОЛЬКО SQL-запросом, без пояснений, без форматирования и без обратных кавычек..
                        """)
                .user("Сгенерируй SQL запрос для: " + message + "\n\nВерни ТОЛЬКО SQL запрос без дополнительного текста.")
                .toolCallbacks(tools.getToolCallbacks())
                .toolNames()
                .call()
                .content();
    }

    public void addDocumentsToKnowledgeBase(List<Map<String, Object>> documents) {
        log.info("Adding {} documents to knowledge base", documents.size());

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
                log.info(String.format("Успешно добавлено %d документов в базу знаний", aiDocuments.size()));
            } else {
                log.error("Error: No valid documents to add");
            }
    }
}
