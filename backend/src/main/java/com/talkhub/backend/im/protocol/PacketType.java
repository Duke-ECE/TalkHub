package com.talkhub.backend.im.protocol;

public enum PacketType {
    AUTH_REQ((byte) 1),
    AUTH_RESP((byte) 2),
    CHAT_REQ((byte) 3),
    CHAT_RESP((byte) 4),
    ACK_REQ((byte) 5),
    PING((byte) 6),
    PONG((byte) 7),
    ERROR((byte) 8);

    private final byte code;

    PacketType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static PacketType fromCode(byte code) {
        for (PacketType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown packet type code: " + code);
    }
}
