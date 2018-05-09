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

public class NamedExecutorService {

	private static final class DelegatingStoppableFuture<V> implements Future<V> {
		private final StoppableRunnable task;
		private final Future<V> f;

		private DelegatingStoppableFuture(StoppableRunnable task, Future<V> f) {
			this.task = task;
			this.f = f;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			long gracePeriod = task.shutdown();
			try {
				f.get(gracePeriod, TimeUnit.MILLISECONDS);
				return true;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			} catch (ExecutionException e) {
				return true;
			} catch (TimeoutException e) {
				return f.cancel(mayInterruptIfRunning);
			}
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			return f.get();
		}

		@Override
		public V get(long timeout, TimeUnit unit)
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

	private ThreadFactoryBuilder baseFactory = new ThreadFactoryBuilder().setDaemon(true);
	
	private Map<String, ExecutorService> factories = new HashMap<>();

	private List<Future<?>> tasks = new ArrayList<>();

	public Future<?> launch(String name, Runnable task) {
		Future<?> f = doLaunch(name, task);
		tasks.add(f);
		return doLaunch(name, task);
	}
	
	public Future<?> launch(String name, StoppableRunnable task) {
		Future<?> f = doLaunch(name, task);
		tasks.add(f);
		return new DelegatingStoppableFuture(task, f);
	}
	
	private Future<?> doLaunch(String name, Runnable task) {
		Objects.requireNonNull(task);
		if (!factories.containsKey(name)) {
			factories.put(name, Executors.newCachedThreadPool(baseFactory.setNameFormat(name+"-%d").build()));
		}
		Future<?> future = factories.get(name).submit(task);
		return future;
	}

	public void shutdown() {
        for(Future<?> subTask : tasks) {
        	subTask.cancel(true);
        }
	}
	
}
