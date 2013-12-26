package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates non daemon threads
 */
class AppServerThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger count = new AtomicInteger(0);
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private final AppService appService;


    AppServerThreadFactory(ThreadGroup group, Thread.UncaughtExceptionHandler uncaughtExceptionHandler, AppService appService) {
        this.group = group;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        this.appService = appService;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(group.getName() + " : " + appService.getAppServer().get().getName() + " " + count.incrementAndGet());
        thread.setDaemon(false);
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return thread;
    }
}
