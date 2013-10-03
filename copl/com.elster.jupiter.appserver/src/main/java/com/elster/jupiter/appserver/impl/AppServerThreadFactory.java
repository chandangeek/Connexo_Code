package com.elster.jupiter.appserver.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates non daemon threads
 */
class AppServerThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger count = new AtomicInteger(0);
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;


    AppServerThreadFactory(ThreadGroup group, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.group = group;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(group.getName() + " : " + Bus.getAppServer().get().getName() + " " + count.incrementAndGet());
        thread.setDaemon(false);
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return thread;
    }
}
