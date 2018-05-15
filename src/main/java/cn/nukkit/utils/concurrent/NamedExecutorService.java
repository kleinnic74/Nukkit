package cn.nukkit.utils.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cn.nukkit.utils.MainLogger;

public class NamedExecutorService {

	private static final class DelegatingStoppableFuture<V> implements Future<V> {
		private final StoppableRunnable task;
		private final Future<V> f;

		private DelegatingStoppableFuture(final StoppableRunnable task, final Future<V> f) {
			this.task = task;
			this.f = f;
		}

		@Override
		public boolean cancel(final boolean mayInterruptIfRunning) {
			final long gracePeriod = task.shutdown();
			try {
				f.get(gracePeriod, TimeUnit.MILLISECONDS);
				return true;
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			} catch (final ExecutionException e) {
				return true;
			} catch (final TimeoutException e) {
				return f.cancel(mayInterruptIfRunning);
			}
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			return f.get();
		}

		@Override
		public V get(final long timeout, final TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return f.get(timeout, unit);
		}

		@Override
		public boolean isCancelled() {
			return f.isCancelled();
		}

		@Override
		public boolean isDone() {
			return f.isDone();
		}
	}

	private final ThreadFactoryBuilder baseFactory = new ThreadFactoryBuilder().setDaemon(true);

	private final Map<String, ExecutorService> factories = new HashMap<>();

	private final List<Future<?>> tasks = new ArrayList<>();

	public Future<?> launch(final String name, final Runnable task) {
		final Future<?> f = doLaunch(name, task);
		tasks.add(f);
		return doLaunch(name, task);
	}

	public Future<?> launch(final String name, final StoppableRunnable task) {
		final Future<?> f = doLaunch(name, wrap(task));
		tasks.add(f);
		return new DelegatingStoppableFuture(task, f);
	}

	private Runnable wrap(final Runnable task) {
		return () -> {
			try {
				MainLogger.getLogger().info(String.format("Starting thread %s...", Thread.currentThread().getName()));
				task.run();
				MainLogger.getLogger().info(String.format("Thread %s terminated", Thread.currentThread().getName()));
			} catch (final Throwable t) {
				MainLogger.getLogger().critical(
						String.format("Uncaught exception in thread %s: ", Thread.currentThread().getName()), t);
			}
		};
	}

	private Future<?> doLaunch(final String name, final Runnable task) {
		Objects.requireNonNull(task);
		if (!factories.containsKey(name)) {
			factories.put(name, Executors.newCachedThreadPool(baseFactory.setNameFormat(name + "-%d").build()));
		}
		final Future<?> future = factories.get(name).submit(task);
		return future;
	}

	public void shutdown() {
		for (final Future<?> subTask : tasks) {
			subTask.cancel(true);
		}
	}

}
