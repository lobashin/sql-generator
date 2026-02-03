package org.example.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.app.model.DocumentInfo;
import org.example.app.service.McpClient;
import org.example.app.service.VectorStoreInitializer;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
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

    private final VectorStore vectorStore;
    private final Resource file;
    private final String category;
    private final String initializerType;

    public FileBasedVectorStoreInitializer(
            Resource file,
            String category,
            String initializerType,
            VectorStore vectorStore) {
        this.vectorStore = vectorStore;
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

            List<DocumentInfo> documents =
                    parseDocumentInfos(readFileContent(file), file.getFilename());

            log.info("[{}] Найдено {} документов для добавления",
                    initializerType, documents.size());

            // Подготавливаем все документы для отправки
            List<Map<String, Object>> documentsToSend = prepareDocumentsForSending(documents);

            // Отправляем все документы одним пакетом
            List<Document> aiDocuments = new ArrayList<>();

            for (Map<String, Object> doc : documentsToSend) {
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
        
        // Если контент пустой, возвращаем пустой список
        if (content == null || content.trim().isEmpty()) {
            return documents;
        }

        String[] lines = content.split("\n");

        StringBuilder currentContent = new StringBuilder();
        StringBuilder metadataContent = new StringBuilder();
        boolean inMetadata = false;
        boolean metadataProcessed = false;

        for (String line : lines) {
            String trimmedLine = line.trim();
            
            if (trimmedLine.equals("--- METADATA ---")) {
                // Если у нас уже есть накопленный контент (предыдущий документ), сохраняем его
                if (currentContent.length() > 0 || metadataContent.length() > 0) {
                    documents.add(createDocumentInfo(
                            currentContent.toString().trim(),
                            metadataContent.toString(),
                            filename
                    ));
                    currentContent = new StringBuilder();
                    metadataContent = new StringBuilder();
                }
                
                inMetadata = true;
                metadataProcessed = false;
                continue;
            }

            if (trimmedLine.equals("--- CONTENT ---")) {
                // Переключаемся на контент
                inMetadata = false;
                metadataProcessed = true;
                continue;
            }

            if (inMetadata) {
                metadataContent.append(line).append("\n");
            } else {
                currentContent.append(line).append("\n");
            }
        }

        // Обрабатываем последний документ
        if (currentContent.length() > 0 || metadataContent.length() > 0) {
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
