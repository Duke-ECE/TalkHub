package com.talkhub.backend.im.session;

import com.talkhub.backend.config.AppProperties;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisSessionRegistry implements SessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(RedisSessionRegistry.class);

    private final StringRedisTemplate redisTemplate;
    private final Duration sessionTtl;
    private final Map<String, Long> channelUsers = new ConcurrentHashMap<>();
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    public RedisSessionRegistry(StringRedisTemplate redisTemplate, AppProperties appProperties) {
        this.redisTemplate = redisTemplate;
        this.sessionTtl = Duration.ofSeconds(appProperties.getIm().getSessionTtlSeconds());
    }

    @Override
    public void register(Long userId, String username, Channel channel) {
        channel.attr(ImAttributes.USER_ID).set(userId);
        channel.attr(ImAttributes.USERNAME).set(username);
        channelUsers.put(channel.id().asLongText(), userId);
        channels.put(channel.id().asLongText(), channel);
        writeSession(userId, channel.id().asLongText(), username);
    }

    @Override
    public void touch(Channel channel) {
        Long userId = channel.attr(ImAttributes.USER_ID).get();
        String username = channel.attr(ImAttributes.USERNAME).get();
        if (userId != null) {
            channels.put(channel.id().asLongText(), channel);
            writeSession(userId, channel.id().asLongText(), username);
        }
    }

    @Override
    public void unregister(Channel channel) {
        String channelId = channel.id().asLongText();
        Long userId = channelUsers.remove(channelId);
        channels.remove(channelId);
        if (userId == null) {
            return;
        }

        try {
            redisTemplate.delete(sessionKey(userId));
        } catch (DataAccessException ex) {
            log.warn("Failed to delete IM session from Redis for userId={}", userId, ex);
        }
    }

    @Override
    public Collection<Channel> authenticatedChannels() {
        return channels.values();
    }

    @Override
    public List<OnlineUserView> onlineUsers() {
        return channelUsers.entrySet().stream()
            .collect(
                ConcurrentHashMap<Long, String>::new,
                (acc, entry) -> {
                    Channel channel = channels.get(entry.getKey());
                    if (channel == null) {
                        return;
                    }
                    String username = channel.attr(ImAttributes.USERNAME).get();
                    acc.put(entry.getValue(), username == null ? "" : username);
                },
                ConcurrentHashMap::putAll
            )
            .entrySet().stream()
            .map(entry -> new OnlineUserView(entry.getKey(), entry.getValue()))
            .sorted((left, right) -> left.username().compareToIgnoreCase(right.username()))
            .toList();
    }

    private void writeSession(Long userId, String channelId, String username) {
        try {
            redisTemplate.opsForHash().putAll(
                sessionKey(userId),
                Map.of(
                    "userId", String.valueOf(userId),
                    "username", username == null ? "" : username,
                    "channelId", channelId
                )
            );
            redisTemplate.expire(sessionKey(userId), sessionTtl);
        } catch (DataAccessException ex) {
            log.warn("Failed to persist IM session to Redis for userId={}", userId, ex);
        }
    }

    private String sessionKey(Long userId) {
        return "talkhub:im:session:user:" + userId;
    }
}
