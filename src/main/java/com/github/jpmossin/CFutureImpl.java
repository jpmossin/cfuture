package com.github.jpmossin;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

final class CFutureImpl<T> implements CFuture<T> {

    private final ExecutorService executor;
    private volatile DoneNotificationTask<T> task;

    private final AtomicReference<T> resultHolder = new AtomicReference<>();
    private final AtomicReference<Exception> errorHolder = new AtomicReference<>();

    private final Set<OnDoneSubscription<T>> onSuccessSubscriptions = ConcurrentHashMap.newKeySet();
    private final Set<OnDoneSubscription<Exception>> onFailureSubscriptions = ConcurrentHashMap.newKeySet();


    CFutureImpl(ExecutorService executor) {
        this.executor = executor;
    }

    void setCodeToRun(Callable<T> futureCode) {
        this.task = new DoneNotificationTask<>(futureCode, this::onDone);
        executor.submit(task);
    }

    @Override
    public <R> CFuture<R> map(Function<? super T, ? extends R> mapper) {
        return map(mapper, executor);
    }

    @Override
    public <R> CFuture<R> map(Function<? super T, ? extends R> mapper, ExecutorService executor) {
        CFutureImpl<R> mappedFuture = new CFutureImpl<>(executor);
        Consumer<T> resultListener = result -> {
            mappedFuture.setCodeToRun(() -> mapper.apply(result));
        };
        OnDoneSubscription<T> subscription = new OnDoneSubscription<>(resultListener, this.executor);
        saveOrInvokeSuccessSubscription(subscription);
        return mappedFuture;
    }

    @Override
    public void forEach(Consumer<T> handler) {
        forEach(handler, executor);
    }

    @Override
    public void forEach(Consumer<T> handler, ExecutorService executor) {
        OnDoneSubscription<T> subscription = new OnDoneSubscription<>(handler, executor);
        saveOrInvokeSuccessSubscription(subscription);
    }

    @Override
    public void onFailure(Consumer<Exception> failureHandler) {
        onFailure(failureHandler, executor);
    }

    @Override
    public void onFailure(Consumer<Exception> failureHandler, ExecutorService executor) {
        OnDoneSubscription<Exception> subscription = new OnDoneSubscription<>(failureHandler, executor);
        saveOrInvokeFailureSubscription(subscription);
    }

    private void saveOrInvokeSuccessSubscription(OnDoneSubscription<T> subscription) {
        saveOrInvokeSubscription(subscription, resultHolder, onSuccessSubscriptions);
    }

    private void saveOrInvokeFailureSubscription(OnDoneSubscription<Exception> subscription) {
        saveOrInvokeSubscription(subscription, errorHolder, onFailureSubscriptions);
    }

    private <R> void saveOrInvokeSubscription(OnDoneSubscription<R> subscription, AtomicReference<R> valueHolder, Set<OnDoneSubscription<R>> savedSubscriptions) {
        R callBackValue;
        synchronized (this) {
            callBackValue = valueHolder.get();
            if (callBackValue == null) {
                savedSubscriptions.add(subscription);
            }
        }
        if (callBackValue != null) {
            callSubscriptionHandler(callBackValue, subscription);
        }

    }

    private void onDone() {
        try {
            T taskResult = task.get();
            resultHolder.set(taskResult);
            callSubscriptionHandlers(onSuccessSubscriptions, taskResult);
        }
        catch (Exception e) {
            errorHolder.set(e);
            callSubscriptionHandlers(onFailureSubscriptions, e);
        }
    }

    private <R> void callSubscriptionHandlers(Set<OnDoneSubscription<R>> subscriptions, R result) {
        for (OnDoneSubscription<R> subscription : subscriptions) {
            callSubscriptionHandler(result, subscription);
        }
    }

    private <R> void callSubscriptionHandler(R result, OnDoneSubscription<R> subscription) {
        ExecutorService executor = subscription.executor;
        Consumer<R> handler = subscription.subscriptionHandler;
        executor.submit(() -> handler.accept(result));
    }


    private static class OnDoneSubscription<R> { // R = T | Exception

        final Consumer<R> subscriptionHandler;
        final ExecutorService executor;

        OnDoneSubscription(Consumer<R> subscriptionHandler, ExecutorService executor) {
            this.subscriptionHandler = subscriptionHandler;
            this.executor = executor;
        }
    }
}
