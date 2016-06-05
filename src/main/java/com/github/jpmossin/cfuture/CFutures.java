package com.github.jpmossin.cfuture;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;

final public class CFutures {

    private static final ExecutorService defaultExecutor = ForkJoinPool.commonPool();

    public static <T> CFuture<T> resolved(T value) {
        return resolved(value, defaultExecutor);
    }

    public static <T> CFuture<T> resolved(T value, ExecutorService executor) {
        CFutureImpl<T> future = new CFutureImpl<>(executor);
        future.completeSuccessfully(value);
        return future;
    }

    public static <T> CFuture<T> from(Callable<T> futureCode) {
        return from(futureCode, defaultExecutor);
    }

    public static <T> CFuture<T> from(Callable<T> futureCode, ExecutorService executor) {
        CFutureImpl<T> future = new CFutureImpl<>(executor);
        DoneNotificationTask<T> task = new DoneNotificationTask<>(futureCode, future);
        executor.submit(task);
        return future;
    }

    public static <T> Promise<T> promise() {
        return new PromiseImpl<>(defaultExecutor);
    }

    public static <T> Promise<T> promise(ExecutorService executor) {
        return new PromiseImpl<>(executor);
    }

}
