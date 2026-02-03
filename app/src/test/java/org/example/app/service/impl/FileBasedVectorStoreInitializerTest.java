package org.example.app.service.impl;

import org.example.app.service.McpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileBasedVectorStoreInitializerTest {

    @Mock
    private McpClient mcpClient;

    @Mock
    private VectorStore vectorStore;

    @Captor
    private ArgumentCaptor<List<Map<String, Object>>> documentsCaptor;

    private FileBasedVectorStoreInitializer initializer;
    private Resource testResource;
    private final String category = "test-category";
    private final String initializerType = "test-type";

    @BeforeEach
    void setUp() {
        // По умолчанию создаем пустой ресурс
        testResource = new ByteArrayResource("".getBytes(), "test.txt");
    }

    @Test
    void initializeFromFile_WhenFileDoesNotExist_ShouldLogWarningAndReturn() {
        // Arrange
        Resource nonExistentResource = mock(Resource.class);
        when(nonExistentResource.exists()).thenReturn(false);
        when(nonExistentResource.getFilename()).thenReturn("non-existent.txt");

        initializer = new FileBasedVectorStoreInitializer(
                nonExistentResource, category, initializerType, vectorStore);

        // Act
        initializer.initializeFromFile();

        // Assert
        verify(mcpClient, never()).addDocumentsToKnowledgeBase(anyList());
    }

    @Test
    void initializeFromFile_WithValidContent_ShouldParseAndSendDocuments() {
        // Arrange
        String content = """
                --- METADATA ---
                title: Test Document 1
                author: Test Author
                --- CONTENT ---
                This is the content of document 1.
                
                --- METADATA ---
                title: Test Document 2
                priority: 5
                --- CONTENT ---
                This is the content of document 2.
                """;

        testResource = new ByteArrayResource(content.getBytes(), "test.txt");
        initializer = new FileBasedVectorStoreInitializer(
                mcpClient, testResource, category, initializerType);

        // Act
        initializer.initializeFromFile();

        // Assert
        verify(mcpClient).addDocumentsToKnowledgeBase(documentsCaptor.capture());
        List<Map<String, Object>> sentDocuments = documentsCaptor.getValue();

        assertEquals(2, sentDocuments.size());

        // Проверяем первый документ
        Map<String, Object> doc1 = sentDocuments.get(0);
        assertEquals("This is the content of document 1.", doc1.get("content"));
        Map<String, Object> metadata1 = (Map<String, Object>) doc1.get("metadata");
        assertEquals("Test Document 1", metadata1.get("title"));
        assertEquals("Test Author", metadata1.get("author"));
        assertEquals(category, metadata1.get("category"));
        assertEquals(initializerType, metadata1.get("type"));
//        assertEquals("test.txt", metadata1.get("source_file"));
        assertNotNull(metadata1.get("timestamp"));

        // Проверяем второй документ
        Map<String, Object> doc2 = sentDocuments.get(1);
        assertEquals("This is the content of document 2.", doc2.get("content"));
        Map<String, Object> metadata2 = (Map<String, Object>) doc2.get("metadata");
        assertEquals("Test Document 2", metadata2.get("title"));
        assertEquals(5, metadata2.get("priority"));
        assertEquals(category, metadata2.get("category"));
        assertEquals(initializerType, metadata2.get("type"));
    }

    @Test
    void initializeFromFile_WithEmptyContent_ShouldSendEmptyList() {
        // Arrange
        String content = "";
        testResource = new ByteArrayResource(content.getBytes(), "empty.txt");
        initializer = new FileBasedVectorStoreInitializer(
                mcpClient, testResource, category, initializerType);

        // Act
        initializer.initializeFromFile();

        // Assert
        verify(mcpClient).addDocumentsToKnowledgeBase(documentsCaptor.capture());
        List<Map<String, Object>> sentDocuments = documentsCaptor.getValue();
        assertTrue(sentDocuments.isEmpty());
    }

    @Test
    void initializeFromFile_WithContentOnlyNoMetadata_ShouldParseCorrectly() {
        // Arrange
        String content = "Just some content without metadata markers.";
        testResource = new ByteArrayResource(content.getBytes(), "content-only.txt");
        initializer = new FileBasedVectorStoreInitializer(
                mcpClient, testResource, category, initializerType);

        // Act
        initializer.initializeFromFile();

        // Assert
        verify(mcpClient).addDocumentsToKnowledgeBase(documentsCaptor.capture());
        List<Map<String, Object>> sentDocuments = documentsCaptor.getValue();

        assertEquals(1, sentDocuments.size());
        Map<String, Object> doc = sentDocuments.get(0);
        assertEquals("Just some content without metadata markers.", doc.get("content"));
        Map<String, Object> metadata = (Map<String, Object>) doc.get("metadata");
        assertEquals(category, metadata.get("category"));
        assertEquals(initializerType, metadata.get("type"));
    }

    @Test
    void initializeFromFile_WithMetadataOnlyNoContentMarker_ShouldParseCorrectly() {
        // Arrange
        String content = """
                --- METADATA ---
                title: Metadata Only
                version: 1.0
                """;

        testResource = new ByteArrayResource(content.getBytes(), "metadata-only.txt");
        initializer = new FileBasedVectorStoreInitializer(
                mcpClient, testResource, category, initializerType);

        // Act
        initializer.initializeFromFile();

        // Assert
        verify(mcpClient).addDocumentsToKnowledgeBase(documentsCaptor.capture());
        List<Map<String, Object>> sentDocuments = documentsCaptor.getValue();

        assertEquals(1, sentDocuments.size());
        Map<String, Object> doc = sentDocuments.get(0);
        assertEquals("", doc.get("content")); // Пустой контент
        Map<String, Object> metadata = (Map<String, Object>) doc.get("metadata");
        assertEquals("Metadata Only", metadata.get("title"));
        assertEquals(1.0, metadata.get("version"));
    }

    @Test
    void initializeFromFile_WithBooleanAndNumericMetadata_ShouldParseCorrectly() {
        // Arrange
        String content = """
                --- METADATA ---
                active: true
                count: 42
                price: 19.99
                --- CONTENT ---
                Content with various metadata types.
                """;

        testResource = new ByteArrayResource(content.getBytes(), "typed-metadata.txt");
        initializer = new FileBasedVectorStoreInitializer(
                mcpClient, testResource, category, initializerType);

        // Act
        initializer.initializeFromFile();

        // Assert
        verify(mcpClient).addDocumentsToKnowledgeBase(documentsCaptor.capture());
        List<Map<String, Object>> sentDocuments = documentsCaptor.getValue();

        assertEquals(1, sentDocuments.size());
        Map<String, Object> metadata = (Map<String, Object>) sentDocuments.get(0).get("metadata");
        
        assertTrue((Boolean) metadata.get("active"));
        assertEquals(42, metadata.get("count"));
        assertEquals(19.99, metadata.get("price"));
    }

    @Test
    void initializeFromFile_WhenMcpClientThrowsException_ShouldThrowRuntimeException() {
        // Arrange
        String content = "Test content";
        testResource = new ByteArrayResource(content.getBytes(), "error.txt");
        initializer = new FileBasedVectorStoreInitializer(
                mcpClient, testResource, category, initializerType);

        doThrow(new RuntimeException("MCP Client error"))
                .when(mcpClient).addDocumentsToKnowledgeBase(anyList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> initializer.initializeFromFile());

        assertEquals("Ошибка инициализации test-type", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("MCP Client error", exception.getCause().getMessage());
    }

    @Test
    void initializeFromFile_WithMultipleDocuments_ShouldMaintainOrder() {
        // Arrange
        String content = """
                --- METADATA ---
                order: 1
                --- CONTENT ---
                First document
                
                --- METADATA ---
                order: 2
                --- CONTENT ---
                Second document
                
                --- METADATA ---
                order: 3
                --- CONTENT ---
                Third document
                """;

        testResource = new ByteArrayResource(content.getBytes(), "ordered.txt");
        initializer = new FileBasedVectorStoreInitializer(
                mcpClient, testResource, category, initializerType);

        // Act
        initializer.initializeFromFile();

        // Assert
        verify(mcpClient).addDocumentsToKnowledgeBase(documentsCaptor.capture());
        List<Map<String, Object>> sentDocuments = documentsCaptor.getValue();

        assertEquals(3, sentDocuments.size());
        
        // Проверяем порядок
        Map<String, Object> doc1 = sentDocuments.get(0);
        assertEquals("First document", doc1.get("content"));
        Map<String, Object> metadata1 = (Map<String, Object>) doc1.get("metadata");
        assertEquals(1, metadata1.get("order"));

        Map<String, Object> doc2 = sentDocuments.get(1);
        assertEquals("Second document", doc2.get("content"));
        Map<String, Object> metadata2 = (Map<String, Object>) doc2.get("metadata");
        assertEquals(2, metadata2.get("order"));

        Map<String, Object> doc3 = sentDocuments.get(2);
        assertEquals("Third document", doc3.get("content"));
        Map<String, Object> metadata3 = (Map<String, Object>) doc3.get("metadata");
        assertEquals(3, metadata3.get("order"));
    }

    @Test
    void initializeFromFile_WithMalformedMetadata_ShouldSkipInvalidLines() {
        // Arrange
        String content = """
                --- METADATA ---
                valid: value
                malformed line without colon
                another: valid
                --- CONTENT ---
                Content with malformed metadata.
                """;

        testResource = new ByteArrayResource(content.getBytes(), "malformed.txt");
        initializer = new FileBasedVectorStoreInitializer(
                mcpClient, testResource, category, initializerType);

        // Act
        initializer.initializeFromFile();

        // Assert
        verify(mcpClient).addDocumentsToKnowledgeBase(documentsCaptor.capture());
        List<Map<String, Object>> sentDocuments = documentsCaptor.getValue();

        assertEquals(1, sentDocuments.size());
        Map<String, Object> metadata = (Map<String, Object>) sentDocuments.get(0).get("metadata");
        
        // Проверяем, что только валидные метаданные были обработаны
        assertEquals("value", metadata.get("valid"));
        assertEquals("valid", metadata.get("another"));
        assertNull(metadata.get("malformed line without colon"));
    }
}
