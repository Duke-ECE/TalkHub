package com.talkhub.backend.im.session;

import io.netty.channel.Channel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnMissingBean(type = "org.springframework.data.redis.core.StringRedisTemplate")
public class InMemorySessionRegistry implements SessionRegistry {

    private final Map<String, Channel> channels = new ConcurrentHashMap<>();
    private final Map<Long, String> users = new ConcurrentHashMap<>();

    @Override
    public void register(Long userId, String username, Channel channel) {
        channel.attr(ImAttributes.USER_ID).set(userId);
        channel.attr(ImAttributes.USERNAME).set(username);
        channels.put(channel.id().asLongText(), channel);
        users.put(userId, username);
    }

    @Override
    public void touch(Channel channel) {
        if (channel.attr(ImAttributes.USER_ID).get() != null) {
            channels.put(channel.id().asLongText(), channel);
        }
    }

    @Override
    public void unregister(Channel channel) {
        channels.remove(channel.id().asLongText());
        Long userId = channel.attr(ImAttributes.USER_ID).get();
        if (userId == null) {
            return;
        }

        boolean stillConnected = channels.values().stream()
            .anyMatch(registered -> userId.equals(registered.attr(ImAttributes.USER_ID).get()));
        if (!stillConnected) {
            users.remove(userId);
        }
    }

    @Override
    public Collection<Channel> authenticatedChannels() {
        return channels.values();
    }

    @Override
    public List<OnlineUserView> onlineUsers() {
        return users.entrySet().stream()
            .map(entry -> new OnlineUserView(entry.getKey(), entry.getValue()))
            .sorted((left, right) -> left.username().compareToIgnoreCase(right.username()))
            .toList();
    }
}
