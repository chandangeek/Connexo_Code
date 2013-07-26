package com.elster.jupiter.appserver.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MessageHandlerTaskExecutorService extends ThreadPoolExecutor {

    public MessageHandlerTaskExecutorService(int threadCount) {
        super(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        if (runnable instanceof MessageHandlerTask) {
            return ((MessageHandlerTask) runnable).newTask(value);
        }
        return super.newTaskFor(runnable, value);
    }
}
