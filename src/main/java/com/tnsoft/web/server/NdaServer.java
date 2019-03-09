package com.tnsoft.web.server;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 硬件通讯服务器，采用Netty框架开发
 */
public class NdaServer {

    private DefaultChannelGroup allChannels = new DefaultChannelGroup("NdaServer");
    private ChannelFactory serverFactory;
    private AtomicBoolean stopped = new AtomicBoolean(false);
    private final int port;

    public NdaServer(int port) {
        this.port = port;
    }

    /**
     * 启动服务器
     */
    public void start() {
        stopped.set(false);

        // 具体步骤按Netty API
        Executor executor = Executors.newCachedThreadPool();
        serverFactory = new NioServerSocketChannelFactory(executor, executor);
        ServerBootstrap sb = new ServerBootstrap(serverFactory);

        sb.setPipelineFactory(new PipelineFactory());
        Channel channel = sb.bind(new InetSocketAddress(port));
        allChannels.add(channel);
    }

    public boolean isRunning() {
        return !stopped.get();
    }

    /**
     * 关闭服务器并释放资源
     */
    public void stop() {
        if (stopped.get()) {
            return;
        }
        stopped.set(true);

        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly(10 * 1000);
        serverFactory.releaseExternalResources();
        allChannels.clear();
    }

    private static final class PipelineFactory implements ChannelPipelineFactory {

        public PipelineFactory() {
        }

        @Override
        public ChannelPipeline getPipeline() {
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            NdaHandler handler = (NdaHandler) wac.getBean("ndaHandler");
            ChannelPipeline p = Channels.pipeline();
            p.addLast("decoder", new NdaDecoder());
            p.addLast("encoder", new NdaEncoder());
            p.addLast("handler", handler);
            return p;
        }
    }

}
