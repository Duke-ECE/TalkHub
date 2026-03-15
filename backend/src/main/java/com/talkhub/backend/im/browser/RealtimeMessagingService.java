package com.talkhub.backend.im.browser;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.talkhub.backend.im.protocol.ImPacket;
import com.talkhub.backend.im.protocol.PacketType;
import com.talkhub.backend.im.session.SessionRegistry;
import com.talkhub.backend.message.MessageService;
import com.talkhub.backend.message.MessageView;
import io.netty.channel.Channel;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RealtimeMessagingService {

    private final MessageService messageService;
    private final SessionRegistry sessionRegistry;
    private final BrowserSessionBridge browserSessionBridge;
    private final OnlineUserDirectory onlineUserDirectory;

    public RealtimeMessagingService(
        MessageService messageService,
        SessionRegistry sessionRegistry,
        BrowserSessionBridge browserSessionBridge,
        OnlineUserDirectory onlineUserDirectory
    ) {
        this.messageService = messageService;
        this.sessionRegistry = sessionRegistry;
        this.browserSessionBridge = browserSessionBridge;
        this.onlineUserDirectory = onlineUserDirectory;
    }

    public MessageView createAndBroadcastMessage(String messageId, Long channelId, Long senderId, String content, long requestId) {
        MessageView saved = messageService.createMessage(messageId, channelId, senderId, content);
        ObjectNode payload = toChatPayload(saved);
        for (Channel channel : sessionRegistry.authenticatedChannels()) {
            channel.writeAndFlush(new ImPacket(
                PacketType.CHAT_RESP,
                requestId,
                saved.messageId(),
                Instant.now().toEpochMilli(),
                payload.deepCopy()
            ));
        }
        browserSessionBridge.broadcastChat(channelId, payload);
        return saved;
    }

    public void broadcastPresence() {
        browserSessionBridge.broadcastPresence(onlineUserDirectory.listOnlineUsers());
    }

    @EventListener
    public void handleBrowserSessionsChanged(BrowserSessionsChangedEvent ignored) {
        broadcastPresence();
    }

    public String nextMessageId() {
        return UUID.randomUUID().toString();
    }

    private ObjectNode toChatPayload(MessageView saved) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("id", saved.id());
        payload.put("messageId", saved.messageId());
        payload.put("channelId", saved.channelId());
        payload.put("senderId", saved.senderId());
        payload.put("senderUsername", saved.senderUsername());
        payload.put("content", saved.content());
        payload.put("status", saved.status());
        payload.put("createdAt", saved.createdAt().toString());
        return payload;
    }
}
