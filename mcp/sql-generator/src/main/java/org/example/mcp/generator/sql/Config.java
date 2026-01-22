package org.example.mcp.generator.sql;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ChatClient chatClient(ChatClientCreator chatClientCreator){
        return chatClientCreator.create();
    }
}
