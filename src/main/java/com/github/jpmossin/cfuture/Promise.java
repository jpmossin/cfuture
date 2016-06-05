package com.github.jpmossin.cfuture;

public interface Promise<T> {

    CFuture<T> future();

    void success(T value);

    void failure(Throwable err);

}
