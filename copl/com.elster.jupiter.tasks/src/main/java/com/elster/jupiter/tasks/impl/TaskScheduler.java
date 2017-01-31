/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

final class TaskScheduler implements Runnable {

    private final Semaphore scheduleNowRequests = new Semaphore(0);
    private final TaskOccurrenceLauncher taskOccurrenceLauncher;
    private final int period;
    private final TimeUnit timeUnit;
    private final ScheduledExecutorService scheduledExecutorService;

    TaskScheduler(TaskOccurrenceLauncher taskOccurrenceLauncher, int period, TimeUnit timeUnit, Function<ThreadFactory, ScheduledExecutorService> builder) {
        this.taskOccurrenceLauncher = taskOccurrenceLauncher;
        this.period = period;
        this.timeUnit = timeUnit;
        scheduledExecutorService = builder.apply(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("TaskOccurrenceLauncher");
                return thread;
            }
        });
    }

    @Override
    public void run() {
        scheduledExecutorService.scheduleAtFixedRate(taskOccurrenceLauncher, 0, period, timeUnit);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                scheduleNowRequests.acquire();
                scheduledExecutorService.schedule(taskOccurrenceLauncher, 0, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        scheduledExecutorService.shutdown();
    }

    public void scheduleNow() {
        scheduleNowRequests.release();
    }

}
