package com.github.jpmossin.cfuture;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronousExecutor implements ExecutorService {

    public int numberOfSubmits = 0;

    public Future<?> submit(Runnable task) {
        task.run();
        numberOfSubmits += 1;
        return CompletableFuture.completedFuture(null);
    }

    public void shutdown() {
    }

    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    public boolean isShutdown() {
        return false;
    }

    public boolean isTerminated() {
        return false;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new RuntimeException("Not supported");
    }

    public <T> Future<T> submit(Runnable task, T result) {
        throw new RuntimeException("Not supported");
    }

    public <T> Future<T> submit(Callable<T> task) {
        throw new RuntimeException("Not supported");
    }


    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new RuntimeException("Not supported");
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        throw new RuntimeException("Not supported");
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new RuntimeException("Not supported");
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new RuntimeException("Not supported");
    }

    public void execute(Runnable command) {
        throw new RuntimeException("Not supported");
    }
}
