package org.example.app.service.vectorstore.initialaizer.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.app.model.DocumentInfo;
import org.example.app.restclient.McpClient;
import org.example.app.service.vectorstore.initialaizer.VectorStoreInitializer;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FileBasedVectorStoreInitializer implements VectorStoreInitializer {

    private final McpClient mcpClient;
    private final Resource file;
    private final String category;
    private final String initializerType;

    public FileBasedVectorStoreInitializer(
            McpClient mcpClient,
            Resource file,
            String category,
            String initializerType) {
        this.mcpClient = mcpClient;
        this.file = file;
        this.category = category;
        this.initializerType = initializerType;
    }

    @Override
    public void initializeFromFile() {
        log.info("Инициализация {} из файла: {}",
                initializerType, file.getFilename());

        try {
            if (!file.exists()) {
                log.warn("Файл не существует: {}", file.getFilename());
                return;
            }

            String content = readFileContent(file);
            List<DocumentInfo> documents =
                    parseDocumentInfos(content, file.getFilename());

            log.info("[{}] Найдено {} документов для добавления",
                    initializerType, documents.size());

            // Подготавливаем все документы для отправки
            List<Map<String, Object>> documentsToSend = prepareDocumentsForSending(documents);

            // Отправляем все документы одним пакетом
            mcpClient.addDocumentsToKnowledgeBase(documentsToSend);
        } catch (Exception e) {
            log.error("Критическая ошибка инициализации {}: {}",
                    initializerType, e.getMessage(), e);
            throw new RuntimeException("Ошибка инициализации " + initializerType, e);
        }
    }

    /**
     * Подготавливает документы для пакетной отправки
     */
    private List<Map<String, Object>> prepareDocumentsForSending(List<DocumentInfo> documents) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (DocumentInfo doc : documents) {
            Map<String, Object> documentMap = new HashMap<>();
            documentMap.put("content", doc.getContent());

            Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
            metadata.put("category", category);
            metadata.put("type", initializerType);
            metadata.put("source_file", file.getFilename());
            metadata.put("timestamp", System.currentTimeMillis());

            documentMap.put("metadata", metadata);
            result.add(documentMap);
        }

        return result;
    }

    private String readFileContent(Resource file) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
                file.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    private List<DocumentInfo> parseDocumentInfos(
            String content, String filename) {

        List<DocumentInfo> documents = new ArrayList<>();
        String[] lines = content.split("\n");

        StringBuilder currentContent = new StringBuilder();
        boolean inMetadata = false;
        StringBuilder metadataContent = new StringBuilder();

        for (String line : lines) {
            if (line.trim().equals("--- METADATA ---")) {
                inMetadata = true;
                continue;
            }

            if (line.trim().equals("--- CONTENT ---")) {
                // Сохраняем предыдущий документ, если есть
                if (!currentContent.isEmpty()) {
                    documents.add(createDocumentInfo(
                            currentContent.toString().trim(),
                            metadataContent.toString(),
                            filename
                    ));
                }

                // Сбрасываем для нового документа
                currentContent = new StringBuilder();
                metadataContent = new StringBuilder();
                inMetadata = false;
                continue;
            }

            if (inMetadata) {
                metadataContent.append(line).append("\n");
            } else {
                currentContent.append(line).append("\n");
            }
        }

        // Добавляем последний документ
        if (!currentContent.isEmpty()) {
            documents.add(createDocumentInfo(
                    currentContent.toString().trim(),
                    metadataContent.toString(),
                    filename
            ));
        }

        return documents;
    }

    private DocumentInfo createDocumentInfo(
            String content, String metadataText, String filename) {

        Map<String, Object> metadata = parseMetadata(metadataText);
        metadata.put("source", filename);
        return new DocumentInfo(content, metadata);
    }

    private Map<String, Object> parseMetadata(String metadataText) {
        Map<String, Object> metadata = new HashMap<>();

        if (metadataText == null || metadataText.trim().isEmpty()) {
            return metadata;
        }

        String[] lines = metadataText.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (!line.contains(":")) {
                continue;
            }

            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                // Преобразуем специальные значения
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    metadata.put(key, Boolean.parseBoolean(value));
                } else if (value.matches("-?\\d+")) {
                    metadata.put(key, Integer.parseInt(value));
                } else if (value.matches("-?\\d+\\.\\d+")) {
                    metadata.put(key, Double.parseDouble(value));
                } else {
                    metadata.put(key, value);
                }
            }
        }

        return metadata;
    }
}
