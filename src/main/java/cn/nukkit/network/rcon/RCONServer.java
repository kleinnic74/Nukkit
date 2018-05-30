package cn.nukkit.network.rcon;

import cn.nukkit.Server;
import cn.nukkit.lang.BaseLang;
import cn.nukkit.utils.Logger;
import cn.nukkit.utils.concurrent.StoppableRunnable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.*;

import com.google.common.base.Preconditions;

/**
 * Thread that performs all RCON network work. A server.
 *
 * @author Tee7even
 */
public class RCONServer implements StoppableRunnable {
    private static final int SERVERDATA_AUTH = 3;
    private static final int SERVERDATA_AUTH_RESPONSE = 2;
    private static final int SERVERDATA_EXECCOMMAND = 2;
    private static final int SERVERDATA_RESPONSE_VALUE = 0;

    private volatile boolean running;

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private String password;
    private final Set<SocketChannel> rconSessions = new HashSet<>();

    private final List<RCONCommand> receiveQueue = new ArrayList<>();
    private final Map<SocketChannel, List<RCONPacket>> sendQueues = new HashMap<>();
	private Logger logger;
	private BaseLang language;

    public RCONServer(BaseLang language, Logger logger, String address, int port, String password) throws IOException {
    	this.language = Preconditions.checkNotNull(language);
    	this.logger = Preconditions.checkNotNull(logger);
    	Preconditions.checkState(password != null && !password.isEmpty(), "nukkit.server.rcon.emptyPasswordError");
    	this.password = password;

        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.configureBlocking(false);
        this.serverChannel.socket().bind(new InetSocketAddress(address, port));

        this.selector = SelectorProvider.provider().openSelector();
        this.serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
    	this.logger.info(this.language.translateString("nukkit.server.rcon.running", new String[]{address, String.valueOf(port)}));
    }

    public RCONCommand receive() {
        synchronized (this.receiveQueue) {
            if (!this.receiveQueue.isEmpty()) {
                RCONCommand command = this.receiveQueue.get(0);
                this.receiveQueue.remove(0);
                return command;
            }

            return null;
        }
    }

    public void respond(SocketChannel channel, int id, String response) {
        this.send(channel, new RCONPacket(id, SERVERDATA_RESPONSE_VALUE, response.getBytes()));
    }

    public long shutdown() {
        this.running = false;
        this.selector.wakeup();
        return 500L;
    }

    public void run() {
    	this.running = true;
        while (this.running) {
            try {
                synchronized (this.sendQueues) {
                    for (SocketChannel channel : this.sendQueues.keySet()) {
                        channel.keyFor(this.selector).interestOps(SelectionKey.OP_WRITE);
                    }
                }

                this.selector.select();

                Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.socket();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(this.selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        this.read(key);
                    } else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            } catch (BufferUnderflowException exception) {
                //Corrupted packet, ignore
            } catch (Exception exception) {
                Server.getInstance().getLogger().logException(exception);
            }
        }

        try {
            this.serverChannel.keyFor(this.selector).cancel();
            this.serverChannel.close();
            this.selector.close();
        } catch (IOException exception) {
            Server.getInstance().getLogger().logException(exception);
        }

        synchronized (this) {
            this.notify();
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int bytesRead;
        try {
            bytesRead = channel.read(buffer);
        } catch (IOException exception) {
            key.cancel();
            channel.close();
            if (this.rconSessions.contains(channel)) {
                this.rconSessions.remove(channel);
            }
            if (this.sendQueues.containsKey(channel)) {
                this.sendQueues.remove(channel);
            }
            return;
        }

        if (bytesRead == -1) {
            key.cancel();
            channel.close();
            if (this.rconSessions.contains(channel)) {
                this.rconSessions.remove(channel);
            }
            if (this.sendQueues.containsKey(channel)) {
                this.sendQueues.remove(channel);
            }
            return;
        }

        buffer.flip();
        this.handle(channel, new RCONPacket(buffer));
    }

    private void handle(SocketChannel channel, RCONPacket packet) {
        switch (packet.getType()) {
            case SERVERDATA_AUTH:
                byte[] payload = new byte[1];
                payload[0] = 0;

                if (new String(packet.getPayload(), Charset.forName("UTF-8")).equals(this.password)) {
                    this.rconSessions.add(channel);
                    this.send(channel, new RCONPacket(packet.getId(), SERVERDATA_AUTH_RESPONSE, payload));
                    return;
                }

                this.send(channel, new RCONPacket(-1, SERVERDATA_AUTH_RESPONSE, payload));
                break;
            case SERVERDATA_EXECCOMMAND:
                if (!this.rconSessions.contains(channel)) {
                    return;
                }

                String command = new String(packet.getPayload(), Charset.forName("UTF-8")).trim();
                synchronized (this.receiveQueue) {
                    this.receiveQueue.add(new RCONCommand(channel, packet.getId(), command));
                }
                break;
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        synchronized (this.sendQueues) {
            List<RCONPacket> queue = this.sendQueues.get(channel);

            ByteBuffer buffer = queue.get(0).toBuffer();
            try {
                channel.write(buffer);
                queue.remove(0);
            } catch (IOException exception) {
                key.cancel();
                channel.close();
                if (this.rconSessions.contains(channel)) {
                    this.rconSessions.remove(channel);
                }
                if (this.sendQueues.containsKey(channel)) {
                    this.sendQueues.remove(channel);
                }
                return;
            }

            if (queue.isEmpty()) {
                this.sendQueues.remove(channel);
            }

            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void send(SocketChannel channel, RCONPacket packet) {
        if (!channel.keyFor(this.selector).isValid()) {
            return;
        }

        synchronized (this.sendQueues) {
            List<RCONPacket> queue = sendQueues.computeIfAbsent(channel, k -> new ArrayList<>());
            queue.add(packet);
        }

        this.selector.wakeup();
    }
}
