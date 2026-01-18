package org.example.mcp.generator.sql;

import org.springframework.ai.chat.client.ChatClient;

public interface ChatClientCreator {
	ChatClient create();
}
