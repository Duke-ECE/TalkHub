package com.talkhub.backend.im.server;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.talkhub.backend.im.protocol.ImPacket;
import com.talkhub.backend.im.protocol.PacketType;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ChannelHandler.Sharable
public class ImHeartbeatHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleStateEvent) {
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(new ImPacket(
                    PacketType.PING,
                    0L,
                    "server-ping-" + Instant.now().toEpochMilli(),
                    Instant.now().toEpochMilli(),
                    JsonNodeFactory.instance.objectNode()
                ));
                return;
            }
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                ctx.close();
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
