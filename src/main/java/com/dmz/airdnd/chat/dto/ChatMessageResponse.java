package com.dmz.airdnd.chat.dto;

import java.time.LocalDateTime;

import com.dmz.airdnd.chat.domain.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
	private String senderName;
	private String content;
	private LocalDateTime createdAt;

	public static ChatMessageResponse from(ChatMessage message) {
		return ChatMessageResponse.builder()
			.senderName(message.getSender().getLoginId())
			.content(message.getContent())
			.createdAt(message.getSentAt())
			.build();
	}
}
