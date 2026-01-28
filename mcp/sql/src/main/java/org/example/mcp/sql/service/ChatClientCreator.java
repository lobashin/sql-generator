package org.example.mcp.sql.service;

import org.springframework.ai.chat.client.ChatClient;

public interface ChatClientCreator {
	ChatClient create();
}
