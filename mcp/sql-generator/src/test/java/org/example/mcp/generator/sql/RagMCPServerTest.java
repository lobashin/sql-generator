package org.example.mcp.generator.sql;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RagMCPServerTest {

    @Test
    void contextLoads() {
        // Этот тест проверяет, что контекст Spring Boot успешно загружается
        assertNotNull(RagMCPServer.class, "Класс RagMCPServer должен существовать");
    }
}
