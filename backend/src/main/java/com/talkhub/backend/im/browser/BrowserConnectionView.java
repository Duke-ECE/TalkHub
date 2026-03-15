package com.talkhub.backend.im.browser;

public record BrowserConnectionView(
    Long userId,
    String username,
    Long channelId
) {
}
