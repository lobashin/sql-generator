package org.example.app.restclient.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.app.restclient.McpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class McpClientImpl implements McpClient {

    private final RestTemplate restTemplate;
    private final String mcpServiceUrl;

    public McpClientImpl(
            RestTemplate restTemplate,
            @Value("http://localhost:8080") String mcpServiceUrl) {
        this.restTemplate = restTemplate;
        this.mcpServiceUrl = mcpServiceUrl;
    }

    @Override
    public String requestResolveSql(String message) {
        return requestResolveSql(message, null);
    }

    @Override
    public String requestResolveSql(String message, Integer topK) {
        log.info("Requesting SQL resolution for message: {}", message);

        try {
            String url = UriComponentsBuilder.fromHttpUrl(mcpServiceUrl)
                    .path("/ai/rag")
                    .build()
                    .toUriString();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("question", message);
            if (topK != null) {
                requestBody.put("topK", topK);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully received response for message: {}", message);
                return response.getBody();
            } else {
                log.error("Failed to get successful response. Status: {}", response.getStatusCode());
                return "Error: HTTP " + response.getStatusCode();
            }

        } catch (Exception e) {
            log.error("Error calling MCP service for message: {}", message, e);
            return "Error calling MCP service: " + e.getMessage();
        }
    }
}
