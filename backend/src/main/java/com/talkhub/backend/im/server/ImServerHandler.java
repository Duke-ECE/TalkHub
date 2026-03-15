package com.talkhub.backend.im.server;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.talkhub.backend.im.browser.RealtimeMessagingService;
import com.talkhub.backend.im.protocol.ImPacket;
import com.talkhub.backend.im.protocol.PacketType;
import com.talkhub.backend.im.session.ImAttributes;
import com.talkhub.backend.im.session.SessionRegistry;
import com.talkhub.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@ChannelHandler.Sharable
public class ImServerHandler extends SimpleChannelInboundHandler<ImPacket> {

    private final JwtService jwtService;
    private final SessionRegistry sessionRegistry;
    private final RealtimeMessagingService realtimeMessagingService;

    public ImServerHandler(
        JwtService jwtService,
        SessionRegistry sessionRegistry,
        RealtimeMessagingService realtimeMessagingService
    ) {
        this.jwtService = jwtService;
        this.sessionRegistry = sessionRegistry;
        this.realtimeMessagingService = realtimeMessagingService;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionRegistry.unregister(ctx.channel());
        realtimeMessagingService.broadcastPresence();
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ImPacket msg) {
        switch (msg.getPacketType()) {
            case AUTH_REQ -> handleAuth(ctx, msg);
            case PING -> handlePing(ctx, msg);
            case CHAT_REQ -> handleChat(ctx, msg);
            case ACK_REQ -> sessionRegistry.touch(ctx.channel());
            default -> writeError(ctx, msg.getRequestId(), "Unsupported packet type: " + msg.getPacketType());
        }
    }

    private void handleAuth(ChannelHandlerContext ctx, ImPacket msg) {
        String token = msg.getPayload().path("token").asText(null);
        if (token == null || token.isBlank()) {
            writeError(ctx, msg.getRequestId(), "Missing token");
            return;
        }

        try {
            Claims claims = jwtService.parseClaims(token);
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            sessionRegistry.register(userId, username, ctx.channel());
            realtimeMessagingService.broadcastPresence();

            ObjectNode payload = JsonNodeFactory.instance.objectNode();
            payload.put("userId", userId);
            payload.put("username", username);
            payload.put("channelId", ctx.channel().id().asLongText());
            ctx.writeAndFlush(new ImPacket(
                PacketType.AUTH_RESP,
                msg.getRequestId(),
                nextMessageId(),
                Instant.now().toEpochMilli(),
                payload
            ));
        } catch (Exception ex) {
            writeError(ctx, msg.getRequestId(), "Invalid token");
        }
    }

    private void handlePing(ChannelHandlerContext ctx, ImPacket msg) {
        sessionRegistry.touch(ctx.channel());
        ctx.writeAndFlush(new ImPacket(
            PacketType.PONG,
            msg.getRequestId(),
            nextMessageId(),
            Instant.now().toEpochMilli(),
            JsonNodeFactory.instance.objectNode()
        ));
    }

    private void handleChat(ChannelHandlerContext ctx, ImPacket msg) {
        Long userId = ctx.channel().attr(ImAttributes.USER_ID).get();
        if (userId == null) {
            writeError(ctx, msg.getRequestId(), "Unauthenticated connection");
            return;
        }

        Long channelId = msg.getPayload().path("channelId").asLong(1L);
        String content = msg.getPayload().path("content").asText("").trim();
        if (content.isBlank()) {
            writeError(ctx, msg.getRequestId(), "Message content is blank");
            return;
        }

        sessionRegistry.touch(ctx.channel());
        realtimeMessagingService.createAndBroadcastMessage(
            msg.getMessageId() == null || msg.getMessageId().isBlank() ? nextMessageId() : msg.getMessageId(),
            channelId,
            userId,
            content,
            msg.getRequestId()
        );
    }

    private void writeError(ChannelHandlerContext ctx, long requestId, String reason) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("reason", reason);
        ctx.writeAndFlush(new ImPacket(
            PacketType.ERROR,
            requestId,
            nextMessageId(),
            Instant.now().toEpochMilli(),
            payload
        ));
    }

    private String nextMessageId() {
        return UUID.randomUUID().toString();
    }
}
