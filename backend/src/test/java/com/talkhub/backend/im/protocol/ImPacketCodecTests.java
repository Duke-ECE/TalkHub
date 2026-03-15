package com.talkhub.backend.im.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.talkhub.backend.config.AppProperties;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImPacketCodecTests {

    @Test
    void shouldRoundTripPacketWithCompression() {
        AppProperties properties = new AppProperties();
        properties.getIm().setCompressionThresholdBytes(1);

        EmbeddedChannel channel = new EmbeddedChannel(new ImPacketCodec(new ObjectMapper(), properties));
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("token", "jwt-token");

        ImPacket outbound = new ImPacket(PacketType.AUTH_REQ, 7L, "msg-1", 123456789L, payload);
        assertThat(channel.writeOutbound(outbound)).isTrue();

        ByteBuf encoded = channel.readOutbound();
        assertThat(channel.writeInbound(encoded)).isTrue();

        ImPacket inbound = channel.readInbound();
        assertThat(inbound.getPacketType()).isEqualTo(PacketType.AUTH_REQ);
        assertThat(inbound.getRequestId()).isEqualTo(7L);
        assertThat(inbound.getMessageId()).isEqualTo("msg-1");
        assertThat(inbound.getPayload().path("token").asText()).isEqualTo("jwt-token");
    }
}
