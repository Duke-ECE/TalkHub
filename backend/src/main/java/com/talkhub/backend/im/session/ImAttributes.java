package com.talkhub.backend.im.session;

import io.netty.util.AttributeKey;

public final class ImAttributes {

    public static final AttributeKey<Long> USER_ID = AttributeKey.valueOf("talkhub.im.userId");
    public static final AttributeKey<String> USERNAME = AttributeKey.valueOf("talkhub.im.username");

    private ImAttributes() {
    }
}
