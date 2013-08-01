package com.elster.jupiter.appserver.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CancellableTaskExecutorService extends ThreadPoolExecutor {

    public CancellableTaskExecutorService(int threadCount) {
        super(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        if (runnable instanceof ProvidesCancellableFuture) {
            return ((ProvidesCancellableFuture) runnable).newTask(value);
        }
        return super.newTaskFor(runnable, value);
    }
}
