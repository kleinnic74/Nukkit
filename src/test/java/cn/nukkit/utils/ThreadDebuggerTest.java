package cn.nukkit.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ThreadDebuggerTest {

	@Test
	public void testDumpAll() {
		CapturingLogger logger = new CapturingLogger();
		ThreadDebugger debug = ThreadDebugger.build();
		debug.dumpAllThreads(logger);
		Assertions.assertAll(logger.hasMessageRE("All threads"));
	}
}
