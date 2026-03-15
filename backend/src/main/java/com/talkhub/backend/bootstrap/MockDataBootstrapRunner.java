package com.talkhub.backend.bootstrap;

import com.talkhub.backend.config.AppProperties;
import com.talkhub.backend.domain.channel.ChannelEntity;
import com.talkhub.backend.domain.channel.ChannelRepository;
import com.talkhub.backend.domain.user.UserEntity;
import com.talkhub.backend.domain.user.UserRepository;
import com.talkhub.backend.message.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
@ConditionalOnProperty(prefix = "app.mock", name = "enabled", havingValue = "true")
public class MockDataBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MockDataBootstrapRunner.class);

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageService messageService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public MockDataBootstrapRunner(
        AppProperties appProperties,
        UserRepository userRepository,
        ChannelRepository channelRepository,
        MessageService messageService
    ) {
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
        this.messageService = messageService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String rawPassword = appProperties.getMock().getPassword();

        UserEntity lea = upsertUser("lea", "Lea", rawPassword);
        UserEntity mika = upsertUser("mika", "Mika", rawPassword);
        UserEntity sora = upsertUser("sora", "Sora", rawPassword);

        ChannelEntity general = ensureChannel("general", "PUBLIC");
        ChannelEntity product = ensureChannel("product", "PUBLIC");
        ChannelEntity ops = ensureChannel("ops", "PUBLIC");

        seedMessage("mock-general-1", general.getId(), lea.getId(), "大家好，mock 房间已经预热好了。");
        seedMessage("mock-general-2", general.getId(), mika.getId(), "前端可以直接登录看看历史消息、在线态和实时推送。");
        seedMessage("mock-general-3", general.getId(), sora.getId(), "浏览器消息会先进 Spring 适配层，再桥接到 Netty 广播。");
        seedMessage("mock-general-4", general.getId(), lea.getId(), "下一步可以继续压 ACK、重连和未读数。");

        seedMessage("mock-product-1", product.getId(), mika.getId(), "product 频道留给需求讨论和版本排期。");
        seedMessage("mock-product-2", product.getId(), lea.getId(), "MVP 先把 general 跑顺，后面再补频道切换。");

        seedMessage("mock-ops-1", ops.getId(), sora.getId(), "ops 频道用于观察部署、日志和告警。");
        seedMessage("mock-ops-2", ops.getId(), lea.getId(), "本地 mock 启动后，默认口令统一为 mock123456。");

        log.info(
            "Mock data initialized. Users={}, channels={}, password={}",
            List.of("lea", "mika", "sora"),
            List.of("general", "product", "ops"),
            rawPassword
        );
    }

    private UserEntity upsertUser(String username, String nickname, String rawPassword) {
        UserEntity user = userRepository.findByUsername(username).orElseGet(() -> {
            UserEntity created = new UserEntity();
            created.setUsername(username);
            return created;
        });

        user.setNickname(nickname);
        user.setStatus("ACTIVE");
        if (user.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        return userRepository.save(user);
    }

    private ChannelEntity ensureChannel(String name, String type) {
        return channelRepository.findByName(name).orElseGet(() -> {
            ChannelEntity channel = new ChannelEntity();
            channel.setName(name);
            channel.setType(type);
            return channelRepository.save(channel);
        });
    }

    private void seedMessage(String messageId, Long channelId, Long senderId, String content) {
        messageService.createMessage(messageId, channelId, senderId, content);
    }
}
