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
            Optional<AssertionError> assertionError = runAssertionCode(() -> assertThat(res, equalTo(expectedValue)));
            assertionError.ifPresent(assertionErrorRef::set);
            latch.countDown();
        });
        future.onFailure(e -> {
            assertionErrorRef.set(new AssertionError("Future failed, expected successfull completion"));
            latch.countDown();
        });
        checkAssertionResult(assertionErrorRef, latch);
    }

    public static void assertFutureFailedWithError(CFuture<?> future, Class<?> expectedExceptionType) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<AssertionError> assertionErrorRef = new AtomicReference<>();
        future.onFailure(err -> {
            Optional<AssertionError> assertionError = runAssertionCode(() -> assertThat(err.getClass(), equalTo(expectedExceptionType)));
            assertionError.ifPresent(assertionErrorRef::set);
            latch.countDown();
        });
        future.forEach(e -> {
            assertionErrorRef.set(new AssertionError("Future completed successfully, expected failure"));
            latch.countDown();
        });
        checkAssertionResult(assertionErrorRef, latch);
    }

    private static Optional<AssertionError> runAssertionCode(Runnable assertionCode) {
        try {
            assertionCode.run();
        } catch (AssertionError e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    private static void checkAssertionResult(AtomicReference<AssertionError> assertionError, CountDownLatch latch) throws InterruptedException {
        boolean timeout = !latch.await(1, TimeUnit.SECONDS);
        if (timeout) {
            fail("timed out waiting for assertion results");
        }
        if (assertionError.get() != null) {
            throw assertionError.get();
        }
    }

}
