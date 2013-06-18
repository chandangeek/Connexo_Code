package com.elster.jupiter.tasks.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

final class TaskScheduler implements Runnable {

    private final Semaphore scheduleNowRequests = new Semaphore(0);
    private final TaskOccurrenceLauncher taskOccurrenceLauncher;
    private final int period;
    private final TimeUnit timeUnit;
    private final ScheduledExecutorService scheduledExecutorService;

    TaskScheduler(TaskOccurrenceLauncher taskOccurrenceLauncher, int period, TimeUnit timeUnit) {
        this.taskOccurrenceLauncher = taskOccurrenceLauncher;
        this.period = period;
        this.timeUnit = timeUnit;
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
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
    }

    public void scheduleNow() {
        scheduleNowRequests.release();
    }

    public void shutDown() {
        scheduledExecutorService.shutdown();
    }
}
