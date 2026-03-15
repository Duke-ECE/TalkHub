package com.talkhub.backend.im.server;

import com.talkhub.backend.config.AppProperties;
import com.talkhub.backend.im.protocol.ImPacketCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

@Component
public class ImChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final AppProperties appProperties;
    private final ImPacketCodec packetCodec;
    private final ImHeartbeatHandler heartbeatHandler;
    private final ImServerHandler serverHandler;

    public ImChannelInitializer(
        AppProperties appProperties,
        ImPacketCodec packetCodec,
        ImHeartbeatHandler heartbeatHandler,
        ImServerHandler serverHandler
    ) {
        this.appProperties = appProperties;
        this.packetCodec = packetCodec;
        this.heartbeatHandler = heartbeatHandler;
        this.serverHandler = serverHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        AppProperties.Im im = appProperties.getIm();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(im.getMaxFrameLength(), 0, 4, 0, 4));
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        pipeline.addLast(
            "idleStateHandler",
            new IdleStateHandler(im.getReaderIdleSeconds(), im.getWriterIdleSeconds(), 0)
        );
        pipeline.addLast("packetCodec", packetCodec);
        pipeline.addLast("heartbeatHandler", heartbeatHandler);
        pipeline.addLast("serverHandler", serverHandler);
    }
}
