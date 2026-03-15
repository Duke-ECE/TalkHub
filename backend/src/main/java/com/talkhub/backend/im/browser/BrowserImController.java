package com.talkhub.backend.im.browser;

import com.talkhub.backend.domain.channel.ChannelRepository;
import com.talkhub.backend.message.CreateMessageRequest;
import com.talkhub.backend.message.MessageView;
import com.talkhub.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/im/browser")
public class BrowserImController {

    private final JwtService jwtService;
    private final ChannelRepository channelRepository;
    private final BrowserSessionBridge browserSessionBridge;
    private final OnlineUserDirectory onlineUserDirectory;
    private final RealtimeMessagingService realtimeMessagingService;

    public BrowserImController(
        JwtService jwtService,
        ChannelRepository channelRepository,
        BrowserSessionBridge browserSessionBridge,
        OnlineUserDirectory onlineUserDirectory,
        RealtimeMessagingService realtimeMessagingService
    ) {
        this.jwtService = jwtService;
        this.channelRepository = channelRepository;
        this.browserSessionBridge = browserSessionBridge;
        this.onlineUserDirectory = onlineUserDirectory;
        this.realtimeMessagingService = realtimeMessagingService;
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(
        @RequestParam Long channelId,
        @RequestParam String token
    ) {
        Claims claims = parseToken(token);
        ensureChannelExists(channelId);
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        BrowserSessionBridge.BrowserSession session = browserSessionBridge.open(userId, username, channelId);
        browserSessionBridge.sendReady(session, onlineUserDirectory.listOnlineUsers());
        return session.emitter();
    }

    @PostMapping("/channels/{channelId}/messages")
    public ResponseEntity<MessageView> sendMessage(
        @PathVariable Long channelId,
        @Valid @RequestBody CreateMessageRequest request,
        @RequestHeader(AUTHORIZATION) String authorization
    ) {
        Claims claims = parseAuthorization(authorization);
        ensureChannelExists(channelId);
        MessageView saved = realtimeMessagingService.createAndBroadcastMessage(
            realtimeMessagingService.nextMessageId(),
            channelId,
            Long.valueOf(claims.getSubject()),
            request.getContent().trim(),
            0L
        );
        return ResponseEntity.ok(saved);
    }

    private Claims parseAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing Bearer token");
        }
        return parseToken(authorization.substring(7));
    }

    private Claims parseToken(String token) {
        return jwtService.parseClaims(token);
    }

    private void ensureChannelExists(Long channelId) {
        if (channelRepository.findById(channelId).isEmpty()) {
            throw new IllegalArgumentException("Channel not found: " + channelId);
        }
    }
}
