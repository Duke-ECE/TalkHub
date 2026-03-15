package com.talkhub.backend.im.browser;

public enum BrowserEventType {
    READY("ready"),
    CHAT("chat"),
    PRESENCE("presence"),
    ERROR("error");

    private final String value;

    BrowserEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
