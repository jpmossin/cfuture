package com.github.jpmossin;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public interface CFuture<T> {

    <R> CFuture<R> map(Function<? super T, ? extends R> mapper);

    <R> CFuture<R> map(Function<? super T, ? extends R> mapper, ExecutorService executor);

    void forEach(Consumer<T> handler);

    void forEach(Consumer<T> handler, ExecutorService executor);

    void onFailure(Consumer<Exception> failureHandler);

    void onFailure(Consumer<Exception> failureHandler, ExecutorService executor);

}
