package cn.nukkit.utils.concurrent;

public interface StoppableRunnable extends Runnable {
	/**
	 * Asks this runnable to stop gracefully. The returned value
	 * is the grace period to wait before forcefully terminating it.
	 * 
	 * @return grace period in milliseconds
	 */
	long shutdown();
}
