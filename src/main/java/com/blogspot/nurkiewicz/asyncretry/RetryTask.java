package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.exception.AbortRetryException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class RetryTask<V> implements Runnable {

	private final CompletableFuture<V> future = new CompletableFuture<>();
	private final Function<RetryContext, V> userTask;
	private final AsyncRetryContext context;
	private final AsyncRetryExecutor parent;

	public RetryTask(Function<RetryContext, V> userTask, AsyncRetryExecutor parent) {
		this(userTask, new AsyncRetryContext(), parent);
	}

	public RetryTask(Function<RetryContext, V> userTask, AsyncRetryContext context, AsyncRetryExecutor parent) {
		this.userTask = userTask;
		this.context = context;
		this.parent = parent;
	}

	@Override
	public void run() {
		final long startTime = System.currentTimeMillis();
		try {
			final V result = userTask.apply(context);
			future.complete(result);
		} catch(AbortRetryException abortEx) {
			completeExceptionally(context.nextRetry(abortEx));
		} catch(Throwable t) {
			handleThrowable(t, System.currentTimeMillis() - startTime);
		}
	}

	private void handleThrowable(Throwable t, long taskDurationMillis) {
		final AsyncRetryContext nextRetryContext = context.nextRetry(t);
		final RetryPolicy retryPolicy = parent.getRetryPolicy();
		if (retryPolicy.shouldContinue(nextRetryContext)) {
			final long delay = calculateNextDelay(taskDurationMillis, nextRetryContext, retryPolicy);
			retryWithDelay(nextRetryContext, delay);
		} else {
			completeExceptionally(nextRetryContext);
		}
	}

	private long calculateNextDelay(long taskDurationMillis, AsyncRetryContext nextRetryContext, RetryPolicy retryPolicy) {
		final long delay = retryPolicy.delayMillis(nextRetryContext);
		return delay - (parent.isFixedDelay()? taskDurationMillis : 0);
	}

	private void completeExceptionally(AsyncRetryContext nextRetryContext) {
		final Exception ex = new RuntimeException("Too many retries: " + context.getRetryCount(), nextRetryContext.getLastThrowable());
		future.completeExceptionally(ex);
	}

	private void retryWithDelay(AsyncRetryContext nextRetryContext, long delay) {
		final RetryTask<V> nextRetryTask = new RetryTask<>(userTask, nextRetryContext, parent);
		parent.getScheduler().schedule(nextRetryTask, delay, MILLISECONDS);
	}

	public CompletableFuture<V> getFuture() {
		return future;
	}
}