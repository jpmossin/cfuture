package com.github.jpmossin;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


final class DoneNotificationTask<V> extends FutureTask<V> {

    private final Runnable onDone;

    DoneNotificationTask(Callable<V> callable, Runnable onDone) {
        super(callable);
        this.onDone = onDone;
    }

    @Override
    protected void done() {
        super.done();
        onDone.run();
    }
}
