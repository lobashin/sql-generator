package org.example.app.restclient;

import java.util.List;
import java.util.Map;

public interface McpClient {
    String requestResolveSql(String message);

    String requestResolveSql(String message, Integer topK);

    String addDocumentsToKnowledgeBase(List<Map<String, Object>> documents);
}
