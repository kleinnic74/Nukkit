package cn.nukkit.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.nukkit.utils.concurrent.StoppableRunnable;
import cn.nukkit.utils.logging.LoggingBackend;

/**
 * author: MagicDroidX Nukkit
 */
public class MainLogger implements StoppableRunnable, Logger {
	private static MainLogger logger;
	protected final ConcurrentLinkedQueue<String> logBuffer = new ConcurrentLinkedQueue<>();
	protected boolean shutdown = false;
	private boolean isShutdown = false;
	protected LogLevel logLevel = LogLevel.DEFAULT_LEVEL;

	private final List<LoggingBackend> backends = new ArrayList<>();

	public synchronized static MainLogger getLogger() {
		if (logger == null) {
			logger = new MainLogger();
		}
		return logger;
	}
	
	private MainLogger() {
		this(LogLevel.DEFAULT_LEVEL);
	}

	private MainLogger(final LogLevel logLevel) {
		this.logLevel = Objects.requireNonNull(logLevel);
	}

	private MainLogger(final boolean logDebug) {
		this(logDebug ? LogLevel.DEBUG : LogLevel.INFO);
	}

	public void addBackend(final LoggingBackend backend) {
		if (backend != null) {
			backends.add(backend);
		}
	}

	@Override
	public void emergency(final String message) {
		if (logLevel.allows(LogLevel.EMERGENCY))
			this.send(TextFormat.RED + "[EMERGENCY] " + message);
	}

	@Override
	public void alert(final String message) {
		if (logLevel.allows(LogLevel.ALERT))
			this.send(TextFormat.RED + "[ALERT] " + message);
	}

	@Override
	public void critical(final String message) {
		if (logLevel.allows(LogLevel.CRITICAL))
			this.send(TextFormat.RED + "[CRITICAL] " + message);
	}

	@Override
	public void error(final String message) {
		if (logLevel.allows(LogLevel.ERROR))
			this.send(TextFormat.DARK_RED + "[ERROR] " + message);
	}

	@Override
	public void warning(final String message) {
		if (logLevel.allows(LogLevel.WARNING))
			this.send(TextFormat.YELLOW + "[WARNING] " + message);
	}

	@Override
	public void notice(final String message) {
		if (logLevel.allows(LogLevel.NOTICE))
			this.send(TextFormat.AQUA + "[NOTICE] " + message);
	}

	@Override
	public void info(final String message) {
		if (logLevel.allows(LogLevel.INFO))
			this.send(TextFormat.WHITE + "[INFO] " + message);
	}

	@Override
	public void debug(final String message) {
		if (logLevel.allows(LogLevel.DEBUG))
			this.send(TextFormat.GRAY + "[DEBUG] " + message);
	}

	public void setLogDebug(final Boolean logDebug) {
		this.logLevel = logDebug ? LogLevel.DEBUG : LogLevel.INFO;
	}

	@Override
	public void log(final LogLevel level, final String message) {
		level.log(this, message);
	}

	@Override
	public long shutdown() {
		synchronized (this) {
			this.shutdown = true;
			notifyAll();
		}
		return 1000;
	}

	protected void send(final String message) {
		this.send(message, -1);
		synchronized (this) {
			this.notify();
		}
	}

	protected void send(final String message, final int level) {
		logBuffer.add(message);
	}

	@Override
	public void run() {
		do {
			waitForMessage();
			flushBuffer();
		} while (!this.shutdown);

		flushBuffer();
		synchronized (this) {
			this.isShutdown = true;
			this.notify();
		}
	}

	private void waitForMessage() {
		while (logBuffer.isEmpty()) {
			try {
				synchronized (this) {
					wait(25000); // Wait for next message
				}
				Thread.sleep(5); // Buffer for 5ms to reduce back and forth between disk
			} catch (final InterruptedException ignore) {
			}
		}
	}

	private void flushBuffer() {
		final Date now = new Date();
		while (!logBuffer.isEmpty()) {
			final String message = logBuffer.poll();
			if (message != null) {
				backends.forEach(be -> be.log(now, message));
			}
		}
	}

	@Override
	public void emergency(final String message, final Throwable t) {
		this.emergency(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void alert(final String message, final Throwable t) {
		this.alert(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void critical(final String message, final Throwable t) {
		this.critical(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void error(final String message, final Throwable t) {
		this.error(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void warning(final String message, final Throwable t) {
		this.warning(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void notice(final String message, final Throwable t) {
		this.notice(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void info(final String message, final Throwable t) {
		this.info(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void debug(final String message, final Throwable t) {
		this.debug(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void log(final LogLevel level, final String message, final Throwable t) {
		this.log(level, message + "\r\n" + Utils.getExceptionMessage(t));
	}

	public void setLevel(final LogLevel level) {
		if (level != null) {
			log(LogLevel.CRITICAL, "Log level set to "+level.name());
			this.logLevel = level;
		}		
	}

}
