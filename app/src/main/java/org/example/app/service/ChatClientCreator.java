package org.example.app.service;

import org.springframework.ai.chat.client.ChatClient;

public interface ChatClientCreator {
	ChatClient create();
}
