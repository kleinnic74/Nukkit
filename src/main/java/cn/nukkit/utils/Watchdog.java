package cn.nukkit.utils;

import cn.nukkit.Server;
import cn.nukkit.utils.concurrent.StoppableRunnable;

public class Watchdog implements StoppableRunnable {
    private final Server server;
    private final long time;
    public boolean running = true;

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
    	int responding = 0;
        while (this.running && server.isRunning()) {
            long current = server.getNextTick();
            if (current != 0) {
                long diff = System.currentTimeMillis() - current;
                if (diff > time) {
                    if (responding == 0) {
                        MainLogger logger = this.server.getLogger();
                        logger.emergency("--------- Server stopped responding --------- (" + (diff / 1000d) + "s)");
                        logger.emergency("Please report this to nukkit:");
                        logger.emergency(" - https://github.com/NukkitX/Nukkit/issues/new");
                        ThreadDebugger threads = ThreadDebugger.build();
                        threads.dumpAllThreads(logger);
                        threads.dumpDeadlocks(logger);
                        logger.emergency("---------------------------------------------");
                        responding++;
                    } else {
                    	if (responding < 3) {
                            MainLogger logger = this.server.getLogger();
                            logger.emergency("------- Server still not responding --------- (" + (diff / 1000d) + "s)");
                            ThreadDebugger threads = ThreadDebugger.build();
                            threads.dumpAllThreads(logger);
                            threads.dumpDeadlocks(logger);
                            logger.emergency("---------------------------------------------");
                    		responding++;
                    	}
                    }
                } else {
                	if (responding > 0) {
                		server.getLogger().notice("Watchdog: normal server operation resumed");
                	}
                    responding = 0;
                }
            }
            try {
                synchronized (this) {
                    this.wait(Math.max(time / 4, 1000));
                }
            } catch (InterruptedException ignore) {}
        }
    }


}