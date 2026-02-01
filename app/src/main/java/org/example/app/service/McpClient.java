package org.example.app.service;

import java.util.List;
import java.util.Map;

public interface McpClient {
    String requestResolveSql(String message);

    String addDocumentsToKnowledgeBase(List<Map<String, Object>> documents);
}
