package com.github.jpmossin.cfuture.testutil;

import com.github.jpmossin.cfuture.CFuture;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FutureAssertions {

    public static <T> void assertFutureResolvedSuccessfully(CFuture<T> future, T expectedValue) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<AssertionError> assertionErrorRef = new AtomicReference<>();
        future.forEach(res -> {
            Optional<AssertionError> assertionError = runAssertionCode(() -> assertThat(res, equalTo(expectedValue)), latch);
            assertionError.ifPresent(assertionErrorRef::set);
        });
        checkAssertionResult(assertionErrorRef.get(), latch);
    }

    public static void assertFutureFailedWithError(CFuture<?> future, Class<?> expectedExceptionType) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<AssertionError> assertionErrorRef = new AtomicReference<>();
        future.onFailure(err -> {
            Optional<AssertionError> assertionError = runAssertionCode(() -> assertThat(err.getClass(), equalTo(expectedExceptionType)), latch);
            assertionError.ifPresent(assertionErrorRef::set);
        });
        checkAssertionResult(assertionErrorRef.get(), latch);
    }

    private static Optional<AssertionError> runAssertionCode(Runnable assertionCode, CountDownLatch latch) {
        try {
            assertionCode.run();
        } catch (AssertionError e) {
            return Optional.of(e);
        } finally {
            latch.countDown();
        }
        return Optional.empty();
    }

    private static void checkAssertionResult(AssertionError assertionError, CountDownLatch latch) throws InterruptedException {
        boolean timedOut = !latch.await(1, TimeUnit.SECONDS);
        if (timedOut) {
            fail("timed out waiting for assertion code to finish");
        }
        if (assertionError != null) {
            throw assertionError;
        }
    }

}
