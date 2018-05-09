package cn.nukkit.utils;

import cn.nukkit.Server;
import cn.nukkit.utils.concurrent.StoppableRunnable;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public class Watchdog implements StoppableRunnable {
    private final Server server;
    private final long time;
    public boolean running = true;
    private boolean responding = true;

    public Watchdog(Server server, long time) {
        this.server = server;
        this.time = time;
    }

    public long shutdown() {
        running = false;
        synchronized (this) {
            this.notifyAll();
        }
        return 500;
    }

    @Override
    public void run() {
    	this.running = true;
        while (this.running && server.isRunning()) {
            long current = server.getNextTick();
            if (current != 0) {
                long diff = System.currentTimeMillis() - current;
                if (diff > time) {
                    if (responding) {
                        MainLogger logger = this.server.getLogger();
                        logger.emergency("--------- Server stopped responding --------- (" + (diff / 1000d) + "s)");
                        logger.emergency("Please report this to nukkit:");
                        logger.emergency(" - https://github.com/NukkitX/Nukkit/issues/new");
                        logger.emergency("---------------- Main thread ----------------");

                        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
						dumpThread(threadMXBean.getThreadInfo(this.server.getPrimaryThread().getId(), Integer.MAX_VALUE), logger);

                        logger.emergency("---------------- All threads ----------------");
                        ThreadInfo[] threads = threadMXBean.dumpAllThreads(true, true);
                        Map<Long, ThreadInfo> threadsById = new HashMap<>();
                        for (ThreadInfo t : threads) {
                        	threadsById.put(t.getThreadId(), t);
                            dumpThread(t, logger);
                            logger.emergency("---------------------------------------------");
                        }
                        long[] deadlocked = threadMXBean.findDeadlockedThreads();
                        if (deadlocked != null) {
                        	for(long threadId : deadlocked) {
                        		ThreadInfo t = threadsById.get(threadId);
                        		if (t != null) {
                        			logger.emergency("  Thread      : "+t.getThreadName());
                        			logger.emergency("     Lock     :"+t.getLockName());
                        			logger.emergency("     LockOwner:"+t.getLockOwnerName());
                        		}
                        	}
                        } else {
                        	logger.emergency("No deadlocked threads found");
                        }
                        logger.emergency("---------------------------------------------");
                        responding = false;
                    }
                } else {
                    responding = true;
                }
            }
            try {
                synchronized (this) {
                    this.wait(Math.max(time / 4, 1000));
                }
            } catch (InterruptedException ignore) {}
        }
    }

    private static void dumpThread(ThreadInfo thread, Logger logger) {
        logger.emergency("Current Thread: " + thread.getThreadName());
        logger.emergency("\tPID: " + thread.getThreadId() + " | Suspended: " + thread.isSuspended() + " | Native: " + thread.isInNative() + " | State: " + thread.getThreadState());
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
}