package com.talkhub.backend.domain.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByChannel_IdOrderByCreatedAtDesc(Long channelId, Pageable pageable);

    Optional<MessageEntity> findByMessageId(String messageId);
}
