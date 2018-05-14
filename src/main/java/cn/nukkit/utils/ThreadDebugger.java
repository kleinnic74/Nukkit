package cn.nukkit.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Objects;

public class ThreadDebugger {

	public static ThreadDebugger build() {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		return new ThreadDebugger(threadMXBean);
	}
	private static void dumpThread(ThreadInfo thread, Logger logger) {
		logger.emergency("Current Thread: " + thread.getThreadName());
		logger.emergency("\tPID: " + thread.getThreadId() + " | Suspended: " + thread.isSuspended() + " | Native: "
				+ thread.isInNative() + " | State: " + thread.getThreadState());
		// Monitors
		if (thread.getLockedMonitors().length != 0) {
			logger.emergency("\tThread is waiting on monitor(s):");
			for (MonitorInfo monitor : thread.getLockedMonitors()) {
				logger.emergency("\t\tLocked on:" + monitor.getLockedStackFrame());
			}
		}

		logger.emergency("\tStack:");
		for (StackTraceElement stack : thread.getStackTrace()) {
			logger.emergency("\t\t" + stack);
		}
	}

	private ThreadMXBean mx;

	private Map<Long, ThreadInfo> threadsById;

	private ThreadDebugger(ThreadMXBean threadMXBean) {
		this.mx = Objects.requireNonNull(threadMXBean);
		for (ThreadInfo t : mx.dumpAllThreads(true, true)) {
			threadsById.put(t.getThreadId(), t);
		}
	}

	public void dumpAllThreads(Logger logger) {
		logger.emergency("---------------- All threads ----------------");
		for (Map.Entry<Long, ThreadInfo> t : threadsById.entrySet()) {
			dumpThread(t.getValue(), logger);
			logger.emergency("---------------------------------------------");
		}
	}

	public void dumpDeadlocks(MainLogger logger) {
		long[] deadlocked = mx.findDeadlockedThreads();
		if (deadlocked != null) {
			for (long threadId : deadlocked) {
				ThreadInfo t = threadsById.get(threadId);
				if (t != null) {
					logger.emergency("  Thread      : " + t.getThreadName());
					logger.emergency("     Lock     :" + t.getLockName());
					logger.emergency("     LockOwner:" + t.getLockOwnerName());
				}
			}
		} else {
			logger.emergency("No deadlocked threads found");
		}
	}
}
