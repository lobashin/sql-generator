package org.example.mcp.generator.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
public class VectorStoreTest {

    @Autowired
    VectorStore vectorStore;

    @BeforeEach
    void beforeEach() {
        List<String> allIds = Objects.requireNonNull(vectorStore.similaritySearch(SearchRequest.builder()
                        .build()))
                .stream()
                .map(Document::getId)
                .collect(Collectors.toList());
        vectorStore.delete(allIds);
    }

    @Test
    void similaritySearch_shouldFoundOnlyOneMostSimilarSql() {
        //given
        var documents = List.of(
                Document.builder()
                        .id(UUID.randomUUID().toString())
                        .metadata("firstMetadataKey", "firstMetadataValue")
                        .score(0.0)
                        .text("firstText")
                        .build());
        vectorStore.add(documents);

        //when
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query("Покажи sql для поиска сотрудника с детьми и с идетификатором 4 или 3")
                        .topK(10)
                        .similarityThreshold(0.7).build());

        //then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("""
                        SELECT employes.name FROM datamart.employee AS employes
                        LEFT JOIN datamart.children AS childrens
                        ON employes.id = childrens.employee_id
                        WHERE employes.id in (4, 3)
                        ORDER BY employes.id ASC
                        """,
                results.getFirst().getText());

    }
}
