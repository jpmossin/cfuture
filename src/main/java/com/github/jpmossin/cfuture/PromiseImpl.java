package com.github.jpmossin.cfuture;

import java.util.concurrent.ExecutorService;

final class PromiseImpl<T> implements Promise<T> {

    private final CFutureImpl<T> future;

    PromiseImpl(ExecutorService executor) {
        future = new CFutureImpl<>(executor);
    }

    @Override
    public CFuture<T> future() {
        return future;
    }

    @Override
    public void success(T value) {
        future.completeSuccessfully(value);
    }

    @Override
    public void failure(Throwable err) {
        future.completeWithError(err);
    }
}
