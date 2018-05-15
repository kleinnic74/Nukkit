package cn.nukkit.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.function.Executable;

public class CapturingLogger implements Logger {

	private enum Level {
		EMERGENCY, ALERT, CRITICAL, ERROR, WARNING, NOTICE, INFO, DEBUG;

		public static Level from(LogLevel level) {
			switch (level) {
			case ALERT:
				return ALERT;
			case CRITICAL:
				return CRITICAL;
			case DEBUG:
				return DEBUG;
			case EMERGENCY:
				return Level.EMERGENCY;
			case ERROR:
				return ERROR;
			case INFO:
				return INFO;
			case NONE:
				return INFO;
			case NOTICE:
				return NOTICE;
			case WARNING:
				return Level.WARNING;
			default:
				throw new IllegalArgumentException(level.toString());
			}
		}
	}

	private static class Message {
		public final Level level;
		public final String message;

		public Message(Level l, String msg) {
			this.level = l;
			this.message = msg;
		}
	}

	private List<Message> messages = new ArrayList<>();

	@Override
	public void emergency(String message) {
		messages.add(new Message(Level.EMERGENCY, message));
	}

	@Override
	public void alert(String message) {
		messages.add(new Message(Level.ALERT, message));
	}

	@Override
	public void critical(String message) {
		messages.add(new Message(Level.CRITICAL, message));
	}

	@Override
	public void error(String message) {
		messages.add(new Message(Level.ERROR, message));
	}

	@Override
	public void warning(String message) {
		messages.add(new Message(Level.WARNING, message));
	}

	@Override
	public void notice(String message) {
		messages.add(new Message(Level.NOTICE, message));
	}

	@Override
	public void info(String message) {
		messages.add(new Message(Level.NOTICE, message));
	}

	@Override
	public void debug(String message) {
		messages.add(new Message(Level.DEBUG, message));
	}

	@Override
	public void log(LogLevel level, String message) {
		messages.add(new Message(Level.from(level), message));
	}

	@Override
	public void emergency(String message, Throwable t) {
		messages.add(new Message(Level.EMERGENCY, message));
	}

	@Override
	public void alert(String message, Throwable t) {
		messages.add(new Message(Level.ALERT, message));
	}

	@Override
	public void critical(String message, Throwable t) {
		messages.add(new Message(Level.CRITICAL, message));
	}

	@Override
	public void error(String message, Throwable t) {
		messages.add(new Message(Level.ERROR, message));
	}

	@Override
	public void warning(String message, Throwable t) {
		messages.add(new Message(Level.WARNING, message));
	}

	@Override
	public void notice(String message, Throwable t) {
		messages.add(new Message(Level.NOTICE, message));
	}

	@Override
	public void info(String message, Throwable t) {
		messages.add(new Message(Level.INFO, message));
	}

	@Override
	public void debug(String message, Throwable t) {
		messages.add(new Message(Level.DEBUG, message));
	}

	@Override
	public void log(LogLevel level, String message, Throwable t) {
		messages.add(new Message(Level.from(level), message));
	}

	public Executable hasMessageRE(String re) {
		Pattern rexp = Pattern.compile(re);
		return () -> assertTrue(
				messages.stream().map(m -> m.message).map(rexp::matcher).filter(Matcher::find).findFirst().isPresent());
	}

}
