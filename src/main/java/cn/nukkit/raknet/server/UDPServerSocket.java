package cn.nukkit.raknet.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.nukkit.utils.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class UDPServerSocket extends ChannelInboundHandlerAdapter implements AutoCloseable {

    protected final Logger logger;
    protected Bootstrap bootstrap;
    protected Channel channel;

    protected ConcurrentLinkedQueue<DatagramPacket> packets = new ConcurrentLinkedQueue<>();

    public UDPServerSocket(final Logger logger) {
        this(logger, 19132, "0.0.0.0");
    }

    public UDPServerSocket(final Logger logger, final int port) {
        this(logger, port, "0.0.0.0");
    }

    public UDPServerSocket(final Logger logger, final int port, final String interfaz) {
        this.logger = logger;
        try {
            if (Epoll.isAvailable()) {
                bootstrap = new Bootstrap()
                        .channel(EpollDatagramChannel.class)
                        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .handler(this)
                        .group(new EpollEventLoopGroup());
                this.logger.info("Epoll is available. EpollEventLoop will be used.");
            } else {
                bootstrap = new Bootstrap()
                        .group(new NioEventLoopGroup())
                        .channel(NioDatagramChannel.class)
                        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .handler(this);
                this.logger.info("Epoll is unavailable. Reverting to NioEventLoop.");
            }
            channel = bootstrap.bind(interfaz, port).sync().channel();
        } catch (final Exception e) {
            this.logger.critical("**** FAILED TO BIND TO " + interfaz + ":" + port + "!");
            this.logger.critical("Perhaps a server is already running on that port?");
            System.exit(1);
        }
    }

    @Override
	public void close() {
        bootstrap.config().group().shutdownGracefully();
        if (channel != null) {
            channel.close().syncUninterruptibly();
        }
    }

    public void clearPacketQueue() {
        this.packets.clear();
    }

    public DatagramPacket readPacket() throws IOException {
        return this.packets.poll();
    }

    public int writePacket(final byte[] data, final String dest, final int port) throws IOException {
        return this.writePacket(data, new InetSocketAddress(dest, port));
    }

    public int writePacket(final byte[] data, final InetSocketAddress dest) throws IOException {
        channel.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(data), dest));
        return data.length;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        this.packets.add((DatagramPacket) msg);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        this.logger.warning(cause.getMessage(), cause);
    }
}
