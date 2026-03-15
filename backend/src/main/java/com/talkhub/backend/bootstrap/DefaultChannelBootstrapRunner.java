package com.talkhub.backend.bootstrap;

import com.talkhub.backend.domain.channel.ChannelEntity;
import com.talkhub.backend.domain.channel.ChannelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 150)
public class DefaultChannelBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultChannelBootstrapRunner.class);

    private final ChannelRepository channelRepository;

    public DefaultChannelBootstrapRunner(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (channelRepository.findByName("general").isPresent()) {
            return;
        }

        ChannelEntity channel = new ChannelEntity();
        channel.setName("general");
        channel.setType("PUBLIC");
        channelRepository.save(channel);
        log.info("Default channel 'general' initialized.");
    }
}
