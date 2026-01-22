package org.example.app.restclient;

public interface McpClient {
    String requestResolveSql(String message);

    String requestResolveSql(String message, Integer topK);
}
