package com.github.jpmossin.cfuture;

import org.junit.Before;
import org.junit.Test;

public class PromiseTest {

    SynchronousExecutor syncExecutor;

    @Before
    public void setup() {
        syncExecutor = new SynchronousExecutor();
    }

    @Test
    public void testFoo() throws Exception {
        Promise<String> p = CFutures.promise(syncExecutor);
        CFuture<String> future = p.future();
        future.forEach(System.out::println);
        future.onFailure(System.out::println);
        p.failure(new Exception("Hello, err!"));
    }

}
