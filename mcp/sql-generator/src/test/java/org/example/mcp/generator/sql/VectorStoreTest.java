package org.example.mcp.generator.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class VectorStoreTest {

    @Autowired
    VectorStore vectorStore;

    @Test
    void similaritySearch_shouldFoundOnlyOneMostSimilarSql(){


        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query("test")
                        .topK(10)
                        .similarityThreshold(0.7).build());

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("select * from my", results.getFirst().getText());


    }
}
