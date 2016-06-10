package com.github.jpmossin.cfuture;

import org.junit.Test;

import static com.github.jpmossin.cfuture.testutil.FutureAssertions.assertFutureFailedWithError;
import static com.github.jpmossin.cfuture.testutil.FutureAssertions.assertFutureResolvedSuccessfully;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PromiseTest {

    @Test
    public void createPromiseAndResolveSuccessfully() throws Exception {
        Promise<String> p = CFutures.promise();
        p.success(":)");
        assertFutureResolvedSuccessfully(p.future(), ":)");
    }

    @Test
    public void createPromiseAndResolveWithFailure() throws Exception {
        Promise<String> p = CFutures.promise();
        p.failure(new RuntimeException(":("));
        assertFutureFailedWithError(p.future(), RuntimeException.class);
    }

    @Test
    public void futuresCreatedFromPromisesAreRunOnProvidedExecutor() {
        SynchronousExecutor syncExecutor = new SynchronousExecutor();
        Promise<String> p = CFutures.promise(syncExecutor);
        p.future().map(v -> v + v);
        p.success(":)");
        assertThat(syncExecutor.numberOfSubmits, equalTo(1));
    }

    @Test
    public void completingPromiseMultipleTimesIsAllowedButHasNoEffect() throws Exception {
        Promise<Integer> p = CFutures.promise();
        p.success(1);
        p.success(2);
        p.failure(new RuntimeException("foo"));
        assertFutureResolvedSuccessfully(p.future(), 1);

        p = CFutures.promise();
        p.failure(new RuntimeException("fail"));
        p.success(1);
        assertFutureFailedWithError(p.future(), RuntimeException.class);
    }

}
