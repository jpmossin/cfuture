package com.github.jpmossin.cfuture;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


final class DoneNotificationTask<V> extends FutureTask<V> {

    private final CFutureImpl<V> future;

    DoneNotificationTask(Callable<V> callable, CFutureImpl<V> future) {
        super(callable);
        this.future = future;
    }

    @Override
    protected void done() {
        super.done();
        try {
            future.completeSuccessfully(get());
        }
        catch (ExecutionException | InterruptedException e) {
            Throwable t = e;
            if (e instanceof ExecutionException && e.getCause() != null) {
                t = e.getCause();
            }
            future.completeWithError(t);
        }
    }
}
