package com.talkhub.backend.im.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

public class ImPacket {

    private PacketType packetType;
    private long requestId;
    private String messageId;
    private long timestamp;
    private JsonNode payload = NullNode.getInstance();

    public ImPacket() {
    }

    public ImPacket(PacketType packetType, long requestId, String messageId, long timestamp, JsonNode payload) {
        this.packetType = packetType;
        this.requestId = requestId;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.payload = payload == null ? NullNode.getInstance() : payload;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload == null ? NullNode.getInstance() : payload;
    }
}
