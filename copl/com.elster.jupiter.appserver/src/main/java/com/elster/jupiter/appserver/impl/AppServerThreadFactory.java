package com.elster.jupiter.appserver.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates non daemon threads
 */
class AppServerThreadFactory implements ThreadFactory {

    private final String name;
    private final ThreadGroup group;
    private final AtomicInteger count = new AtomicInteger(0);
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;


    AppServerThreadFactory(ThreadGroup group, String name, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.group = group;
        this.name = name;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(group.getName() + " : " + name + " " + count.incrementAndGet());
        thread.setDaemon(false);
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return thread;
    }
}
