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

    private final ChatClient chatClient;
    private final ToolCallbackProvider tools;
    private final List<McpSyncClient> mcpSyncClients;

    public McpClientImpl(ChatClient chatClient,
                         ToolCallbackProvider tools,
                         List<McpSyncClient> mcpSyncClients) {
        this.chatClient = chatClient;
        this.tools = tools;
        this.mcpSyncClients = mcpSyncClients;
    }

    @Override
    public String requestResolveSql(String message) {

        return chatClient
                .prompt()
                .system("""
                        Ты - SQL консультант, который отвечает только SQL-запросом

                        1. Анализируй описание структуры базы данных
                        2. Сначала используй тул [Получить описание структуры базы данных] для получения данных о базе, затем примени [Описание конктертной таблицы] для получения данных о таблице"
                        3. Используй правильные имена таблиц и полей из описания структуры базы данных
                        4. Используй правильные имена столбцов из описание конктертной таблицы
                        """)
                .user("Сгенерируй SQL запрос для: " + message)
                .toolCallbacks(tools.getToolCallbacks()

                        )
                .toolNames()
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
