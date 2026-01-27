package org.example.mcp.sql.config;

import org.example.mcp.sql.service.ChatClientCreator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClientCreator chatClientCreator){
        return chatClientCreator.create();
    }
}
