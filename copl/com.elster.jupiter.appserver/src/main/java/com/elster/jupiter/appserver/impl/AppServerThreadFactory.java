/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Creates non daemon threads
 */
class AppServerThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger count = new AtomicInteger(0);
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private final AppService appService;
    private final Supplier<String> namer;

    AppServerThreadFactory(ThreadGroup group, Thread.UncaughtExceptionHandler uncaughtExceptionHandler, AppService appService, Supplier<String> namer) {
        this.group = group;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        this.appService = appService;
        this.namer = namer;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(group, runnable);
        thread.setName(group.getName() + " : " + appService.getAppServer().get().getName() + ' ' + namer.get() + ' ' + count.incrementAndGet());
        thread.setDaemon(false);
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return thread;
    }
}
