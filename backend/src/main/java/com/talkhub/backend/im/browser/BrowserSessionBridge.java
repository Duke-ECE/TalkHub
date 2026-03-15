package com.talkhub.backend.im.browser;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.talkhub.backend.im.session.OnlineUserView;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BrowserSessionBridge {

    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, BrowserSession> sessions = new ConcurrentHashMap<>();

    public BrowserSessionBridge(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public BrowserSession open(Long userId, String username, Long channelId) {
        BrowserSession session = new BrowserSession(userId, username, channelId, new SseEmitter(0L));
        sessions.put(session.id(), session);
        session.emitter().onCompletion(() -> removeSession(session.id()));
        session.emitter().onTimeout(() -> removeSession(session.id()));
        session.emitter().onError(ex -> removeSession(session.id()));
        eventPublisher.publishEvent(new BrowserSessionsChangedEvent());
        return session;
    }

    public void close(String sessionId) {
        BrowserSession session = sessions.remove(sessionId);
        if (session != null) {
            session.emitter().complete();
            eventPublisher.publishEvent(new BrowserSessionsChangedEvent());
        }
    }

    public List<BrowserConnectionView> activeConnections() {
        return sessions.values().stream()
            .map(session -> new BrowserConnectionView(session.userId(), session.username(), session.channelId()))
            .toList();
    }

    public void broadcastChat(Long channelId, ObjectNode payload) {
        broadcast(channelId, BrowserEventType.CHAT, payload);
    }

    public void broadcastPresence(List<OnlineUserView> onlineUsers) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("timestamp", Instant.now().toEpochMilli());
        payload.set("onlineUsers", JsonNodeFactory.instance.pojoNode(onlineUsers));
        for (BrowserSession session : new ArrayList<>(sessions.values())) {
            send(session, BrowserEventType.PRESENCE, payload);
        }
    }

    public void sendReady(BrowserSession session, List<OnlineUserView> onlineUsers) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("sessionId", session.id());
        payload.put("userId", session.userId());
        payload.put("username", session.username());
        payload.put("channelId", session.channelId());
        payload.put("timestamp", Instant.now().toEpochMilli());
        payload.set("onlineUsers", JsonNodeFactory.instance.pojoNode(onlineUsers));
        send(session, BrowserEventType.READY, payload);
    }

    public void sendError(BrowserSession session, String reason) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("reason", reason);
        payload.put("timestamp", Instant.now().toEpochMilli());
        send(session, BrowserEventType.ERROR, payload);
    }

    private void broadcast(Long channelId, BrowserEventType type, ObjectNode payload) {
        for (BrowserSession session : new ArrayList<>(sessions.values())) {
            if (!channelId.equals(session.channelId())) {
                continue;
            }
            send(session, type, payload);
        }
    }

    private void send(BrowserSession session, BrowserEventType type, ObjectNode payload) {
        try {
            session.emitter().send(SseEmitter.event().name(type.value()).data(payload));
        } catch (IOException ex) {
            removeSession(session.id());
            session.emitter().completeWithError(ex);
        }
    }

    private void removeSession(String sessionId) {
        BrowserSession removed = sessions.remove(sessionId);
        if (removed != null) {
            eventPublisher.publishEvent(new BrowserSessionsChangedEvent());
        }
    }

    public record BrowserSession(
        String id,
        Long userId,
        String username,
        Long channelId,
        SseEmitter emitter
    ) {
        private BrowserSession(Long userId, String username, Long channelId, SseEmitter emitter) {
            this(java.util.UUID.randomUUID().toString(), userId, username, channelId, emitter);
        }
    }
}
