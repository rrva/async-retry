package com.nurkiewicz.asyncretry.policy;

import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/18/13, 11:25 PM
 */
public class RetryPolicyBlackListTest extends AbstractRetryPolicyTest {

	@Test
	public void shouldAbortOnSpecifiedException() throws Exception {
		final RetryPolicy policy = new RetryPolicy().abortOn(ConnectException.class);

		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
	}

	@Test
	public void shouldRetryIfExceptionNotAborting() throws Exception {
		final RetryPolicy policy = new RetryPolicy().abortOn(ConnectException.class);

		assertThat(shouldRetryOn(policy, new Exception())).isTrue();
		assertThat(shouldRetryOn(policy, new RuntimeException())).isTrue();
		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
		assertThat(shouldRetryOn(policy, new SocketException())).isTrue();
		assertThat(shouldRetryOn(policy, new ClassCastException())).isTrue();
		assertThat(shouldRetryOn(policy, new NullPointerException())).isTrue();
		assertThat(shouldRetryOn(policy, new IllegalArgumentException())).isTrue();
		assertThat(shouldRetryOn(policy, new IllegalStateException())).isTrue();
		assertThat(shouldRetryOn(policy, new TimeoutException())).isTrue();
	}

	@Test
	public void shouldRetryIfErrorNotAborting() throws Exception {
		final RetryPolicy policy = new RetryPolicy().abortOn(ConnectException.class);

		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isTrue();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isTrue();
		assertThat(shouldRetryOn(policy, new NoClassDefFoundError())).isTrue();
	}

	@Test
	public void shouldAbortIfBlackListedException() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				abortOn(NullPointerException.class);

		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
	}

	@Test
	public void shouldAbortOnSubclassesOfBlackListedException() throws Exception {
		final RetryPolicy policy = new RetryPolicy().abortOn(IOException.class);

		assertThat(shouldRetryOn(policy, new FileNotFoundException())).isFalse();
		assertThat(shouldRetryOn(policy, new SocketException())).isFalse();
		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
	}

	@Test
	public void shouldAbortOnAnyBlackListedExceptions() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				abortOn(NullPointerException.class).
				abortOn(OutOfMemoryError.class).
				abortOn(StackOverflowError.class);

		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isFalse();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isFalse();
	}

	@Test
	public void shouldAbortOnAnyBlackListedExceptionsInOneList() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				abortOn(NullPointerException.class, OutOfMemoryError.class, StackOverflowError.class);

		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isFalse();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isFalse();
	}

	@Test
	public void shouldAbortOnSubclassesOfAnyOfBlackListedExceptions() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				abortOn(IOException.class).
				abortOn(RuntimeException.class);

		assertThat(shouldRetryOn(policy, new FileNotFoundException())).isFalse();
		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
	}

	@Test
	public void shouldAbortOnSubclassesOfAnyOfBlackListedExceptionsInOneList() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				abortOn(IOException.class, RuntimeException.class);

		assertThat(shouldRetryOn(policy, new FileNotFoundException())).isFalse();
		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
	}

}
