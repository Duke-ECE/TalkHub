package com.talkhub.backend.im.session;

import io.netty.channel.Channel;

import java.util.Collection;
import java.util.List;

public interface SessionRegistry {

    void register(Long userId, String username, Channel channel);

    void touch(Channel channel);

    void unregister(Channel channel);

    Collection<Channel> authenticatedChannels();

    List<OnlineUserView> onlineUsers();
}
