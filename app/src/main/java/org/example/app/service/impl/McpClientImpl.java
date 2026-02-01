package org.example.app.service.impl;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.example.app.service.McpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class McpClientImpl implements McpClient {

    public static final Logger log = LoggerFactory.getLogger(McpClientImpl.class);

    private final RestTemplate restTemplate;
    private final ChatClient chatClient;
    private final ToolCallbackProvider tools;
    private final List<McpSyncClient> mcpSyncClients;

    public McpClientImpl(RestTemplate restTemplate,
                         ChatClient chatClient,
                         ToolCallbackProvider tools,
                         List<McpSyncClient> mcpSyncClients) {
        this.restTemplate = restTemplate;
        this.chatClient = chatClient;
        this.tools = tools;
        this.mcpSyncClients = mcpSyncClients;
    }

    @Override
    public String requestResolveSql(String message) {
        log.info("Requesting SQL resolution for message: {}", message);

        return chatClient.prompt().system("""
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
                        """).user("Сгенерируй SQL запрос для: " + message + "\n\nВерни ТОЛЬКО SQL запрос без дополнительного текста.")
                .toolCallbacks(Arrays.stream(tools.getToolCallbacks()).toList().stream().filter(toolCallback -> "query_knowledge_base".equals(toolCallback.getToolDefinition().name())).toList())
                .call()
                .content();
    }

    public String addDocumentsToKnowledgeBase(List<Map<String, Object>> documents) {
        log.info("Adding {} documents to knowledge base", documents.size());

        try {
            var response = mcpSyncClients
                    .getFirst()
                    .callTool(McpSchema.CallToolRequest.builder()
                            .name("add_to_knowledge_base")
                            .arguments(Map.of("documents", ModelOptionsUtils.toJsonString(documents)))
                            .build());
            return response.content().stream().map(Object::toString).collect(Collectors.joining("\n---\n"));
        } catch (Exception e) {
            log.error("Error adding documents to knowledge base", e);
            return "Error adding documents: " + e.getMessage();
        }
    }
}
