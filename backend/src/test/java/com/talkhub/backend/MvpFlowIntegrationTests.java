package com.talkhub.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.talkhub.backend.im.protocol.ImPacket;
import com.talkhub.backend.im.protocol.ImPacketCodec;
import com.talkhub.backend.im.protocol.PacketType;
import com.talkhub.backend.im.server.ImServerLifecycle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "app.im.enabled=true",
        "app.im.host=127.0.0.1",
        "app.im.port=0",
        "app.database.auto-create-if-missing=false",
        "app.jwt-secret=test-secret",
        "app.admin.username=admin",
        "app.admin.password=admin123456"
    }
)
class MvpFlowIntegrationTests {

    @LocalServerPort
    private int apiPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ImServerLifecycle imServerLifecycle;

    @Autowired
    private ObjectMapper objectMapper;

    private NioEventLoopGroup eventLoopGroup;

    @AfterEach
    void cleanup() {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
    }

    @Test
    void shouldLoginAuthenticateOverImSendMessageAndFetchHistory() throws Exception {
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            "http://localhost:" + apiPort + "/api/auth/login",
            Map.of("username", "admin", "password", "admin123456"),
            Map.class
        );
        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String token = String.valueOf(loginResponse.getBody().get("token"));

        BlockingQueue<ImPacket> inboundPackets = new LinkedBlockingQueue<>();
        eventLoopGroup = new NioEventLoopGroup(1);
        ImPacketCodec clientCodec = new ImPacketCodec(objectMapper, new TestAppProperties());

        var channel = new Bootstrap()
            .group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                    ch.pipeline().addLast(new LengthFieldPrepender(4));
                    ch.pipeline().addLast(clientCodec);
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<ImPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, ImPacket msg) {
                            inboundPackets.offer(msg);
                        }
                    });
                }
            })
            .connect("127.0.0.1", imServerLifecycle.getBoundPort())
            .sync()
            .channel();

        channel.writeAndFlush(new ImPacket(
            PacketType.AUTH_REQ,
            1L,
            "auth-1",
            Instant.now().toEpochMilli(),
            JsonNodeFactory.instance.objectNode().put("token", token)
        )).sync();

        ImPacket authResp = inboundPackets.poll(5, TimeUnit.SECONDS);
        assertThat(authResp).isNotNull();
        assertThat(authResp.getPacketType()).isEqualTo(PacketType.AUTH_RESP);

        channel.writeAndFlush(new ImPacket(
            PacketType.CHAT_REQ,
            2L,
            "chat-1",
            Instant.now().toEpochMilli(),
            JsonNodeFactory.instance.objectNode()
                .put("channelId", 1L)
                .put("content", "hello netty mvp")
        )).sync();

        ImPacket chatResp = inboundPackets.poll(5, TimeUnit.SECONDS);
        assertThat(chatResp).isNotNull();
        assertThat(chatResp.getPacketType()).isEqualTo(PacketType.CHAT_RESP);
        assertThat(chatResp.getPayload().path("content").asText()).isEqualTo("hello netty mvp");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<JsonNode> historyResponse = restTemplate.exchange(
            "http://localhost:" + apiPort + "/api/channels/1/messages?limit=10",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            JsonNode.class
        );

        assertThat(historyResponse.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode messages = historyResponse.getBody();
        assertThat(messages).isNotNull();
        assertThat(messages.isArray()).isTrue();
        assertThat(messages.get(0).path("content").asText()).isEqualTo("hello netty mvp");

        channel.close().sync();
    }

    @Test
    void shouldCreateMessageOverHttpAndFetchItFromHistory() {
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            "http://localhost:" + apiPort + "/api/auth/login",
            Map.of("username", "admin", "password", "admin123456"),
            Map.class
        );
        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String token = String.valueOf(loginResponse.getBody().get("token"));

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setBearerAuth(token);
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> createResponse = restTemplate.exchange(
            "http://localhost:" + apiPort + "/api/channels/1/messages",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("content", "hello http debug"), postHeaders),
            JsonNode.class
        );

        assertThat(createResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().path("content").asText()).isEqualTo("hello http debug");

        HttpHeaders getHeaders = new HttpHeaders();
        getHeaders.setBearerAuth(token);
        getHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<JsonNode> historyResponse = restTemplate.exchange(
            "http://localhost:" + apiPort + "/api/channels/1/messages?limit=10",
            HttpMethod.GET,
            new HttpEntity<>(getHeaders),
            JsonNode.class
        );

        assertThat(historyResponse.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode messages = historyResponse.getBody();
        assertThat(messages).isNotNull();
        assertThat(messages.isArray()).isTrue();
        assertThat(messages.toString()).contains("hello http debug");
    }

    @Test
    void shouldReturnStructuredValidationErrorWhenHttpMessageIsBlank() {
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            "http://localhost:" + apiPort + "/api/auth/login",
            Map.of("username", "admin", "password", "admin123456"),
            Map.class
        );
        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String token = String.valueOf(loginResponse.getBody().get("token"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            "http://localhost:" + apiPort + "/api/channels/1/messages",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("content", "   "), headers),
            JsonNode.class
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().path("code").asText()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldListOnlineUsersForChannelAfterImAuthentication() throws Exception {
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            "http://localhost:" + apiPort + "/api/auth/login",
            Map.of("username", "admin", "password", "admin123456"),
            Map.class
        );
        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String token = String.valueOf(loginResponse.getBody().get("token"));

        BlockingQueue<ImPacket> inboundPackets = new LinkedBlockingQueue<>();
        eventLoopGroup = new NioEventLoopGroup(1);
        ImPacketCodec clientCodec = new ImPacketCodec(objectMapper, new TestAppProperties());

        var channel = new Bootstrap()
            .group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                    ch.pipeline().addLast(new LengthFieldPrepender(4));
                    ch.pipeline().addLast(clientCodec);
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<ImPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, ImPacket msg) {
                            inboundPackets.offer(msg);
                        }
                    });
                }
            })
            .connect("127.0.0.1", imServerLifecycle.getBoundPort())
            .sync()
            .channel();

        channel.writeAndFlush(new ImPacket(
            PacketType.AUTH_REQ,
            1L,
            "auth-online-1",
            Instant.now().toEpochMilli(),
            JsonNodeFactory.instance.objectNode().put("token", token)
        )).sync();

        ImPacket authResp = inboundPackets.poll(5, TimeUnit.SECONDS);
        assertThat(authResp).isNotNull();
        assertThat(authResp.getPacketType()).isEqualTo(PacketType.AUTH_RESP);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            "http://localhost:" + apiPort + "/api/channels/1/online-users",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            JsonNode.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isArray()).isTrue();
        assertThat(response.getBody().toString()).contains("\"username\":\"admin\"");

        channel.close().sync();
    }

    private static final class TestAppProperties extends com.talkhub.backend.config.AppProperties {
        private TestAppProperties() {
            getIm().setCompressionThresholdBytes(Integer.MAX_VALUE);
        }
    }
}
