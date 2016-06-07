package com.github.jpmossin.cfuture;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

final class CFutureImpl<T> implements CFuture<T> {

    private final ExecutorService executor;

    private final AtomicReference<T> successResult = new AtomicReference<>();
    private final AtomicReference<Throwable> errorResult = new AtomicReference<>();

    private Set<OnDoneSubscription<T>> onSuccessSubscriptions = new CopyOnWriteArraySet<>();
    private Set<OnDoneSubscription<Throwable>> onFailureSubscriptions = new CopyOnWriteArraySet<>();


    CFutureImpl(ExecutorService executor) {
        this.executor = executor;
    }

    void completeSuccessfully(T result) {
        complete(result, successResult, onSuccessSubscriptions);
    }

    void completeWithError(Throwable err) {
        complete(err, errorResult, onFailureSubscriptions);
    }

    private <V> void complete(V result, AtomicReference<V> resultHolder, Set<OnDoneSubscription<V>> subscriptions) {
        Iterator<OnDoneSubscription<V>> subscriptionsSnapshot;
        synchronized (this) {
            resultHolder.set(result);
            subscriptionsSnapshot = subscriptions.iterator();
            onSuccessSubscriptions = null;
            onFailureSubscriptions = null;
        }
        callSubscriptionHandlers(subscriptionsSnapshot, result);
    }

    @Override
    public <R> CFuture<R> map(Function<? super T, ? extends R> mapper) {
        return map(mapper, executor);
    }

    @Override
    public <R> CFuture<R> map(Function<? super T, ? extends R> mapper, ExecutorService executor) {
        return createDerivedFuture(mapper,  CFutureImpl::completeSuccessfully, executor);
    }

    @Override
    public <R> CFuture<R> flatMap(Function<? super T, CFuture<? extends R>> mapper) {
        return flatMap(mapper, executor);
    }

    @Override
    public <R> CFuture<R> flatMap(Function<? super T, CFuture<? extends R>> mapper, ExecutorService executor) {
        return createDerivedFuture(mapper, (resultFuture, mappedFuture) -> {
            mappedFuture.forEach(resultFuture::completeSuccessfully);
            mappedFuture.onFailure(resultFuture::completeWithError);
        }, executor);
    }

    private <R, V> CFuture<R> createDerivedFuture(Function<? super T, V> mapper, BiConsumer<CFutureImpl<R>, V> successCompleter, ExecutorService executor) {
        CFutureImpl<R> resultFuture = new CFutureImpl<>(executor);
        forEach(res -> {
            V mappedValue = null;
            boolean failed = false;
            try {
                mappedValue = mapper.apply(res);
            } catch (Exception e) {
                failed = true;
                resultFuture.completeWithError(e);
            }
            if (!failed) {
                successCompleter.accept(resultFuture, mappedValue);
            }
        }, executor);
        onFailure(resultFuture::completeWithError);
        return resultFuture;
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
    public void onFailure(Consumer<Throwable> failureHandler) {
        onFailure(failureHandler, executor);
    }

    @Override
    public void onFailure(Consumer<Throwable> failureHandler, ExecutorService executor) {
        OnDoneSubscription<Throwable> subscription = new OnDoneSubscription<>(failureHandler, executor);
        saveOrInvokeFailureSubscription(subscription);
    }

    private void saveOrInvokeSuccessSubscription(OnDoneSubscription<T> subscription) {
        saveOrInvokeSubscription(subscription, successResult, onSuccessSubscriptions);
    }

    private void saveOrInvokeFailureSubscription(OnDoneSubscription<Throwable> subscription) {
        saveOrInvokeSubscription(subscription, errorResult, onFailureSubscriptions);
    }

    private <R> void saveOrInvokeSubscription(OnDoneSubscription<R> subscription, AtomicReference<R> valueHolder, Set<OnDoneSubscription<R>> savedSubscriptions) {
        R callBackValue;
        synchronized (this) {
            callBackValue = valueHolder.get();
            if (callBackValue == null && savedSubscriptions != null) {
                savedSubscriptions.add(subscription);
            }
        }
        if (callBackValue != null) {
            callSubscriptionHandler(callBackValue, subscription);
        }
    }

    private <R> void callSubscriptionHandlers(Iterator<OnDoneSubscription<R>> subscriptions, R result) {
        while (subscriptions.hasNext()) {
            callSubscriptionHandler(result, subscriptions.next());
        }
    }

    private <R> void callSubscriptionHandler(R result, OnDoneSubscription<R> subscription) {
        ExecutorService executor = subscription.executor;
        Consumer<R> handler = subscription.subscriptionHandler;
        executor.submit(() -> handler.accept(result));
    }


    private static class OnDoneSubscription<R> { // R = T | Throwable

        final Consumer<R> subscriptionHandler;
        final ExecutorService executor;

        OnDoneSubscription(Consumer<R> subscriptionHandler, ExecutorService executor) {
            this.subscriptionHandler = subscriptionHandler;
            this.executor = executor;
        }
    }
}
