package com.talkhub.backend.domain.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {

    Optional<ChannelEntity> findByName(String name);
}
