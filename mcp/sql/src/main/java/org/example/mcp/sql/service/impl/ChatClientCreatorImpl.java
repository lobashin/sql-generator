package org.example.mcp.sql.service.impl;

import chat.giga.springai.GigaChatOptions;
import chat.giga.springai.api.chat.GigaChatApi;
import org.example.mcp.sql.service.ChatClientCreator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;

@Service
class ChatClientCreatorImpl implements ChatClientCreator {

	private final ChatClient.Builder chatClientBuilder;

	ChatClientCreatorImpl(ChatClient.Builder chatClientBuilder) {
		this.chatClientBuilder = chatClientBuilder;
	}

	@Override
	public ChatClient create() {
		return chatClientBuilder
				.defaultAdvisors(
						new SimpleLoggerAdvisor())
				.defaultOptions(GigaChatOptions.builder()
						.model(GigaChatApi.ChatModel.GIGA_CHAT_2)
						.build())
				.build();
	}
}
