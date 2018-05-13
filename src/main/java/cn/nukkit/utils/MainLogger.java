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

	private List<LoggingBackend> backends = new ArrayList<>();

	public synchronized static MainLogger getLogger() {
		if (logger == null) {
			logger = new MainLogger();
		}
		return logger;
	}
	
	private MainLogger() {
		this(LogLevel.DEFAULT_LEVEL);
	}

	private MainLogger(LogLevel logLevel) {
		this.logLevel = Objects.requireNonNull(logLevel);
	}

	private MainLogger(boolean logDebug) {
		this(logDebug ? LogLevel.DEBUG : LogLevel.INFO);
	}

	public void addBackend(LoggingBackend backend) {
		if (backend != null) {
			backends.add(backend);
		}
	}

	@Override
	public void emergency(String message) {
		if (LogLevel.EMERGENCY.getLevel() <= logLevel.getLevel())
			this.send(TextFormat.RED + "[EMERGENCY] " + message);
	}

	@Override
	public void alert(String message) {
		if (LogLevel.ALERT.getLevel() <= logLevel.getLevel())
			this.send(TextFormat.RED + "[ALERT] " + message);
	}

	@Override
	public void critical(String message) {
		if (LogLevel.CRITICAL.getLevel() <= logLevel.getLevel())
			this.send(TextFormat.RED + "[CRITICAL] " + message);
	}

	@Override
	public void error(String message) {
		if (LogLevel.ERROR.getLevel() <= logLevel.getLevel())
			this.send(TextFormat.DARK_RED + "[ERROR] " + message);
	}

	@Override
	public void warning(String message) {
		if (LogLevel.WARNING.getLevel() <= logLevel.getLevel())
			this.send(TextFormat.YELLOW + "[WARNING] " + message);
	}

	@Override
	public void notice(String message) {
		if (LogLevel.NOTICE.getLevel() <= logLevel.getLevel())
			this.send(TextFormat.AQUA + "[NOTICE] " + message);
	}

	@Override
	public void info(String message) {
		if (LogLevel.INFO.getLevel() <= logLevel.getLevel())
			this.send(TextFormat.WHITE + "[INFO] " + message);
	}

	@Override
	public void debug(String message) {
		if (LogLevel.DEBUG.getLevel() <= logLevel.getLevel())
			this.send(TextFormat.GRAY + "[DEBUG] " + message);
	}

	public void setLogDebug(Boolean logDebug) {
		this.logLevel = logDebug ? LogLevel.DEBUG : LogLevel.INFO;
	}

	@Override
	public void log(LogLevel level, String message) {
		level.log(this, message);
	}

	public long shutdown() {
		synchronized (this) {
			this.shutdown = true;
			notifyAll();
		}
		return 1000;
	}

	protected void send(String message) {
		this.send(message, -1);
		synchronized (this) {
			this.notify();
		}
	}

	protected void send(String message, int level) {
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
			} catch (InterruptedException ignore) {
			}
		}
	}

	private void flushBuffer() {
		Date now = new Date();
		while (!logBuffer.isEmpty()) {
			String message = logBuffer.poll();
			if (message != null) {
				backends.forEach(be -> be.log(now, message));
			}
		}
	}

	@Override
	public void emergency(String message, Throwable t) {
		this.emergency(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void alert(String message, Throwable t) {
		this.alert(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void critical(String message, Throwable t) {
		this.critical(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void error(String message, Throwable t) {
		this.error(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void warning(String message, Throwable t) {
		this.warning(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void notice(String message, Throwable t) {
		this.notice(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void info(String message, Throwable t) {
		this.info(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void debug(String message, Throwable t) {
		this.debug(message + "\r\n" + Utils.getExceptionMessage(t));
	}

	@Override
	public void log(LogLevel level, String message, Throwable t) {
		this.log(level, message + "\r\n" + Utils.getExceptionMessage(t));
	}

	public void setLevel(LogLevel level) {
		if (level != null) {
			this.logLevel = level;
		}		
	}

}
