package com.talkhub.backend.im.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhub.backend.config.AppProperties;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToMessageCodec;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@ChannelHandler.Sharable
public class ImPacketCodec extends MessageToMessageCodec<ByteBuf, ImPacket> {

    private static final int MAGIC = 0x54484d50;
    private static final byte VERSION = 1;
    private static final byte COMPRESS_NONE = 0;
    private static final byte COMPRESS_GZIP = 1;

    private final ObjectMapper objectMapper;
    private final int compressionThresholdBytes;

    public ImPacketCodec(ObjectMapper objectMapper, AppProperties appProperties) {
        this.objectMapper = objectMapper;
        this.compressionThresholdBytes = appProperties.getIm().getCompressionThresholdBytes();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ImPacket msg, List<Object> out) throws Exception {
        byte[] payloadBytes = objectMapper.writeValueAsBytes(msg.getPayload());
        byte compressFlag = COMPRESS_NONE;
        if (payloadBytes.length >= compressionThresholdBytes) {
            payloadBytes = GzipCompression.compress(payloadBytes);
            compressFlag = COMPRESS_GZIP;
        }

        byte[] messageIdBytes = msg.getMessageId() == null
            ? new byte[0]
            : msg.getMessageId().getBytes(StandardCharsets.UTF_8);

        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(MAGIC);
        buffer.writeByte(VERSION);
        buffer.writeByte(msg.getPacketType().getCode());
        buffer.writeByte(compressFlag);
        buffer.writeLong(msg.getRequestId());
        buffer.writeLong(msg.getTimestamp());
        buffer.writeShort(messageIdBytes.length);
        buffer.writeBytes(messageIdBytes);
        buffer.writeInt(payloadBytes.length);
        buffer.writeBytes(payloadBytes);
        out.add(buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int magic = msg.readInt();
        if (magic != MAGIC) {
            throw new IllegalArgumentException("Unexpected IM packet magic: " + magic);
        }

        msg.readByte();
        PacketType packetType = PacketType.fromCode(msg.readByte());
        byte compressFlag = msg.readByte();
        long requestId = msg.readLong();
        long timestamp = msg.readLong();
        int messageIdLength = msg.readUnsignedShort();
        String messageId = msg.readCharSequence(messageIdLength, StandardCharsets.UTF_8).toString();
        int payloadLength = msg.readInt();
        byte[] payloadBytes = ByteBufUtil.getBytes(msg.readSlice(payloadLength));
        if (compressFlag == COMPRESS_GZIP) {
            payloadBytes = GzipCompression.decompress(payloadBytes);
        }

        JsonNode payload = payloadBytes.length == 0
            ? objectMapper.getNodeFactory().nullNode()
            : objectMapper.readTree(payloadBytes);

        out.add(new ImPacket(packetType, requestId, messageId, timestamp, payload));
    }
}
