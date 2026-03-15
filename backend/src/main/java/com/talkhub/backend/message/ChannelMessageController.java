package com.talkhub.backend.message;

import com.talkhub.backend.domain.channel.ChannelRepository;
import com.talkhub.backend.im.browser.OnlineUserDirectory;
import com.talkhub.backend.im.session.OnlineUserView;
import com.talkhub.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
public class ChannelMessageController {

    private final MessageService messageService;
    private final JwtService jwtService;
    private final ChannelRepository channelRepository;
    private final OnlineUserDirectory onlineUserDirectory;

    public ChannelMessageController(
        MessageService messageService,
        JwtService jwtService,
        ChannelRepository channelRepository,
        OnlineUserDirectory onlineUserDirectory
    ) {
        this.messageService = messageService;
        this.jwtService = jwtService;
        this.channelRepository = channelRepository;
        this.onlineUserDirectory = onlineUserDirectory;
    }

    @GetMapping("/{channelId}/messages")
    public ResponseEntity<List<MessageView>> getMessages(
        @PathVariable Long channelId,
        @RequestParam(defaultValue = "20") int limit,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        parseAuthorization(authorization);
        ensureChannelExists(channelId);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(messageService.getRecentMessages(channelId, safeLimit));
    }

    @PostMapping("/{channelId}/messages")
    public ResponseEntity<MessageView> createMessage(
        @PathVariable Long channelId,
        @Valid @RequestBody CreateMessageRequest request,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        Claims claims = parseAuthorization(authorization);
        ensureChannelExists(channelId);
        Long userId = Long.valueOf(claims.getSubject());
        MessageView saved = messageService.createMessage(
            UUID.randomUUID().toString(),
            channelId,
            userId,
            request.getContent().trim()
        );
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{channelId}/online-users")
    public ResponseEntity<List<OnlineUserView>> getOnlineUsers(
        @PathVariable Long channelId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        parseAuthorization(authorization);
        ensureChannelExists(channelId);
        return ResponseEntity.ok(onlineUserDirectory.listOnlineUsers());
    }

    private Claims parseAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing Bearer token");
        }
        return jwtService.parseClaims(authorization.substring(7));
    }

    private void ensureChannelExists(Long channelId) {
        if (channelRepository.findById(channelId).isEmpty()) {
            throw new IllegalArgumentException("Channel not found: " + channelId);
        }
    }
}
