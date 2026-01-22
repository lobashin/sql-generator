package org.example.mcp.generator.sql;


import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

@SpringBootTest
class ChatClientTest {

    @Autowired
    ChatClient chatClient;

    @Test
    void prompt_hello(){
        String content = Objects.requireNonNull(chatClient.prompt(Prompt.builder()
                        .content("Привет, тебе пишет sql-generator")
                .build()).call().content());
        System.out.printf(content);
    }

}