package com.talkhub.backend.message;

import java.time.OffsetDateTime;

public record MessageView(
    Long id,
    String messageId,
    Long channelId,
    Long senderId,
    String senderUsername,
    String content,
    String status,
    OffsetDateTime createdAt
) {
}
