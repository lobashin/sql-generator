package org.example.mcp.generator.sql;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RagMCPServerTest {

    @Test
    void contextLoads() {
        // Этот тест проверяет, что контекст Spring Boot успешно загружается
        // Если контекст не загрузится, тест упадет с исключением
    }
}
