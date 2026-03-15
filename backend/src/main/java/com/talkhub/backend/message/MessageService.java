package com.talkhub.backend.message;

import com.talkhub.backend.domain.channel.ChannelEntity;
import com.talkhub.backend.domain.channel.ChannelRepository;
import com.talkhub.backend.domain.message.MessageEntity;
import com.talkhub.backend.domain.message.MessageRepository;
import com.talkhub.backend.domain.user.UserEntity;
import com.talkhub.backend.domain.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    public MessageService(
        MessageRepository messageRepository,
        ChannelRepository channelRepository,
        UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MessageView createMessage(String messageId, Long channelId, Long senderId, String content) {
        MessageEntity existing = messageRepository.findByMessageId(messageId).orElse(null);
        if (existing != null) {
            return toView(existing);
        }

        ChannelEntity channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelId));
        UserEntity sender = userRepository.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderId));

        MessageEntity entity = new MessageEntity();
        entity.setMessageId(messageId);
        entity.setChannel(channel);
        entity.setSender(sender);
        entity.setContent(content);
        entity.setStatus("PERSISTED");
        MessageEntity saved = messageRepository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<MessageView> getRecentMessages(Long channelId, int limit) {
        return messageRepository.findByChannel_IdOrderByCreatedAtDesc(channelId, PageRequest.of(0, limit)).stream()
            .map(this::toView)
            .toList();
    }

    private MessageView toView(MessageEntity entity) {
        return new MessageView(
            entity.getId(),
            entity.getMessageId(),
            entity.getChannel().getId(),
            entity.getSender().getId(),
            entity.getSender().getUsername(),
            entity.getContent(),
            entity.getStatus(),
            entity.getCreatedAt()
        );
    }
}
