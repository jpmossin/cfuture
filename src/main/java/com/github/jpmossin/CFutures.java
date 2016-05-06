package com.github.jpmossin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class CFutures {

    public static <T> CFuture<T> from(Callable<T> futureCode) {
        return from(futureCode, ForkJoinPool.commonPool());
    }

    public static <T> CFuture<T> from(Callable<T> futureCode, ExecutorService executor) {
        CFutureImpl<T> future = new CFutureImpl<>(executor);
        future.setCodeToRun(futureCode);
        return future;
    }

}
