package org.example.app.config;

import org.example.app.service.ChatClientCreator;
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
