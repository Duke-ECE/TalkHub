package com.talkhub.backend.im.server;

import com.talkhub.backend.config.AppProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class ImServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ImServerLifecycle.class);

    private final AppProperties appProperties;
    private final ImChannelInitializer channelInitializer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean running;

    public ImServerLifecycle(AppProperties appProperties, ImChannelInitializer channelInitializer) {
        this.appProperties = appProperties;
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void start() {
        if (running || !appProperties.getIm().isEnabled()) {
            return;
        }

        AppProperties.Im im = appProperties.getIm();
        bossGroup = new NioEventLoopGroup(im.getBossThreads());
        workerGroup = im.getWorkerThreads() > 0
            ? new NioEventLoopGroup(im.getWorkerThreads())
            : new NioEventLoopGroup();

        try {
            serverChannel = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .bind(im.getHost(), im.getPort())
                .sync()
                .channel();
            running = true;
            log.info("Netty IM server started on {}:{}", im.getHost(), getBoundPort());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting Netty IM server", ex);
        } catch (Exception ex) {
            stop();
            throw new IllegalStateException("Failed to start Netty IM server", ex);
        }
    }

    @Override
    public void stop() {
        running = false;
        if (serverChannel != null) {
            serverChannel.close();
            serverChannel = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    public int getBoundPort() {
        if (serverChannel == null || !(serverChannel.localAddress() instanceof InetSocketAddress address)) {
            throw new IllegalStateException("Netty IM server is not bound");
        }
        return address.getPort();
    }
}
