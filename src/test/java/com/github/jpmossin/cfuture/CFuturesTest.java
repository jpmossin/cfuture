package com.github.jpmossin.cfuture;

import org.junit.Before;
import org.junit.Test;

import static com.github.jpmossin.cfuture.testutil.FutureAssertions.assertFutureFailedWithError;
import static com.github.jpmossin.cfuture.testutil.FutureAssertions.assertFutureResolvedSuccessfully;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for the static factory methods in CFutures that create CFuture and Promise objects
 */
public class CFuturesTest {

    SynchronousExecutor syncExecutor;
    
    private static final String hello = "Hello!";

    @Before
    public void setup() {
        syncExecutor = new SynchronousExecutor();
    }

    @Test
    public void createResolvedFuture() throws Exception {
        CFuture<String> helloFuture = CFutures.resolved(hello);
        assertFutureResolvedSuccessfully(helloFuture, hello);
    }

    @Test
    public void createFutureFromRunnable() throws Exception {
        CFuture<String> helloFuture = CFutures.from(() -> hello);
        assertFutureResolvedSuccessfully(helloFuture, hello);
    }

    @Test
    public void createFailingFuture() throws Exception {
        CFuture<Integer> failedFuture = CFutures.from(() -> 1 / 0);
        assertFutureFailedWithError(failedFuture, ArithmeticException.class);
    }

    @Test
    public void futuresAreRunOnProvidedExecutor() {
        CFutures.from(() -> hello, syncExecutor);
        assertThat(syncExecutor.numberOfSubmits, equalTo(1));
    }
    
    @Test
    public void createPromiseAndCheckCorrespondingFutureIsResolved() throws Exception {
        Promise<String> p = CFutures.promise();
        p.success(hello);
        assertFutureResolvedSuccessfully(p.future(), hello);
    }

    @Test
    public void futuresCreatedFromPromisesAreRunOnProvidedExecutor() {
        Promise<String> p = CFutures.promise(syncExecutor);
        p.future().map(v -> v + v);
        p.success(":)");
        assertThat(syncExecutor.numberOfSubmits, equalTo(1));
    }


}