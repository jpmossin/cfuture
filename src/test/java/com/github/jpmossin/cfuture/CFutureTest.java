package com.github.jpmossin.cfuture;

import org.junit.Before;
import org.junit.Test;

import static com.github.jpmossin.cfuture.testutil.FutureAssertions.assertFutureFailedWithError;
import static com.github.jpmossin.cfuture.testutil.FutureAssertions.assertFutureResolvedSuccessfully;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CFutureTest {

    SynchronousExecutor syncExecutor;

    @Before
    public void setup() {
        syncExecutor = new SynchronousExecutor();
    }

    @Test
    public void createResolvedFuture() throws Exception {
        CFuture<String> helloFuture = CFutures.resolved("hello");
        assertFutureResolvedSuccessfully(helloFuture, "hello");
    }

    @Test
    public void createFutureFromRunnable() throws Exception {
        CFuture<String> helloFuture = CFutures.from(() -> "hello");
        assertFutureResolvedSuccessfully(helloFuture, "hello");
    }

    @Test
    public void createFailingFuture() throws Exception {
        CFuture<Integer> failedFuture = CFutures.from(() -> 1 / 0);
        assertFutureFailedWithError(failedFuture, ArithmeticException.class);
    }

    @Test
    public void futuresAreRunOnProvidedExecutor() {
        CFutures.from(() -> "hello", syncExecutor);
        assertThat(syncExecutor.numberOfSubmits, equalTo(1));
    }


    @Test
    public void mapCreatesNewSuccessfullyResolvedFuture() throws Exception {
        CFuture<Integer> two =  CFutures.from(() -> 1).map(one -> one + one);
        assertFutureResolvedSuccessfully(two, 2);
    }

    @Test
    public void mapCreatesNewFailedFutureWhenThrowing() throws Exception {
        CFuture<Integer> inf =  CFutures.from(() -> 0).map(zero -> 1 / zero);
        assertFutureFailedWithError(inf, ArithmeticException.class);
    }

    @Test
    public void mapCausesOneAdditionalSubmitToExecutor() {
        CFutures.resolved(1, syncExecutor)
                .map(one -> one)
                .map(one -> one);
        assertThat(syncExecutor.numberOfSubmits, equalTo(2));
    }

    @Test
    public void testSwithExecutorForMapCall() throws Exception  {
        CFuture<Integer> ten =  CFutures.resolved(1, syncExecutor)
                .map(one -> one + one)
                .map(two -> two * 5, CFutures.defaultExecutor);
        assertFutureResolvedSuccessfully(ten, 10);
        assertThat(syncExecutor.numberOfSubmits, equalTo(1));
    }

    @Test
    public void simpleFlatMapComposition() throws Exception {
        CFuture<Integer> oneF =  CFutures.from(() -> 1);
        CFuture<Integer> threeF =  oneF.flatMap(one -> CFutures.resolved(one + 2));
        assertFutureResolvedSuccessfully(threeF, 3);
    }

    @Test
    public void flatMapWithAlreadyDefinedFuture() throws Exception {
        CFuture<Integer> oneF =  CFutures.resolved(1);
        CFuture<Integer> twoF =  CFutures.resolved(2);
        CFuture<Integer> alsoTwoF =  oneF.flatMap(one -> twoF);
        assertFutureResolvedSuccessfully(alsoTwoF, 2);
    }

    @Test
    public void flatMapCausesOneAdditionalSubmitToExecutor() {
        CFutures.resolved(1, syncExecutor)
                .flatMap(CFutures::resolved)
                .flatMap(CFutures::resolved);
        assertThat(syncExecutor.numberOfSubmits, equalTo(2));
    }

}
