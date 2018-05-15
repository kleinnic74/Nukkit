package cn.nukkit.raknet.server;

import java.util.concurrent.ConcurrentLinkedQueue;

import cn.nukkit.Server;
import cn.nukkit.utils.Logger;

/**
 * author: MagicDroidX Nukkit Project
 */
public class RakNetServer {
	protected final int port;
	protected String interfaz;

	protected Logger logger;

	protected ConcurrentLinkedQueue<byte[]> externalQueue;
	protected ConcurrentLinkedQueue<byte[]> internalQueue;

	public RakNetServer(final Logger logger, final int port) {
		this(logger, port, "0.0.0.0");
	}

	public RakNetServer(final Logger logger, final int port, final String interfaz) {
		this.port = port;
		if (port < 1 || port > 65536) {
			throw new IllegalArgumentException("Invalid port range");
		}

		this.interfaz = interfaz;
		this.logger = logger;

		this.externalQueue = new ConcurrentLinkedQueue<>();
		this.internalQueue = new ConcurrentLinkedQueue<>();

	}

	public int getPort() {
		return port;
	}

	public String getInterface() {
		return interfaz;
	}

	public Logger getLogger() {
		return logger;
	}

	public ConcurrentLinkedQueue<byte[]> getExternalQueue() {
		return externalQueue;
	}

	public ConcurrentLinkedQueue<byte[]> getInternalQueue() {
		return internalQueue;
	}

	public void pushMainToThreadPacket(final byte[] data) {
		this.internalQueue.add(data);
	}

	public byte[] readMainToThreadPacket() {
		return this.internalQueue.poll();
	}

	public void pushThreadToMainPacket(final byte[] data) {
		this.externalQueue.add(data);
	}

	public byte[] readThreadToMainPacket() {
		return this.externalQueue.poll();
	}

	public void run() {
		try (final UDPServerSocket socket = new UDPServerSocket(this.getLogger(), port, this.interfaz)) {
			final SessionManager session = new SessionManager(this, socket);
			try {
				session.run();
			} catch (final Throwable e) {
				Server.getInstance().getLogger().emergency("Exception in RakNetServer", e);
			}
		}
	}
}
