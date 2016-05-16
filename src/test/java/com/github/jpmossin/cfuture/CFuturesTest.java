package com.github.jpmossin.cfuture;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// tests for the factory methods that create CFuture objects
public class CFuturesTest {

    SynchronousExecutor syncExecutor;

    @Before
    public void setup() {
        syncExecutor = new SynchronousExecutor();
    }

    @Test
    public void createResolvedFuture() throws Exception {
        CFuture<String> helloFuture = CFutures.resolved("hello");
        assertFutureResolvedToValue(helloFuture, "hello");
    }

    @Test
    public void createFutureFromRunnable() throws Exception {
        CFuture<String> helloFuture = CFutures.from(() -> "hello");
        assertFutureResolvedToValue(helloFuture, "hello");
    }

    @Test
    public void createFailingFuture() throws Exception {
        CFuture<Integer> failedFuture = CFutures.from(() -> 1 / 0);
        assertFutureFailedWithError(failedFuture, ArithmeticException.class);
    }

    @Test
    public void tasksAreSubmittedOnProvidedExecutor() {
        CFutures.from(() -> "hello", syncExecutor);
        assertThat(syncExecutor.numberOfSubmits, equalTo(1));
    }

    private <T> void assertFutureResolvedToValue(CFuture<T> future, String value) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        future.forEach(v -> {
            assertThat(v, equalTo(value));
            latch.countDown();
        });
        assertTrue("Timeout waiting on successful completion", latch.await(1, TimeUnit.SECONDS));
    }

    private <T> void assertFutureFailedWithError(CFuture<T> future, Class<?> exceptionType) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        future.onFailure(e -> {
            assertThat(e.getClass(), equalTo(exceptionType));
            latch.countDown();
        });
        assertTrue("Timeout waiting for failed completion", latch.await(1, TimeUnit.SECONDS));
    }


}