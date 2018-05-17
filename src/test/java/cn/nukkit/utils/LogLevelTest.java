package cn.nukkit.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LogLevelTest {

	@Test
	public void allows()
	{
		assertTrue(LogLevel.EMERGENCY.allows(LogLevel.EMERGENCY));
		assertFalse(LogLevel.EMERGENCY.allows(LogLevel.ALERT));

		assertTrue(LogLevel.INFO.allows(LogLevel.EMERGENCY));
		assertTrue(LogLevel.INFO.allows(LogLevel.WARNING));
		assertFalse(LogLevel.INFO.allows(LogLevel.DEBUG));
	
	}
}
