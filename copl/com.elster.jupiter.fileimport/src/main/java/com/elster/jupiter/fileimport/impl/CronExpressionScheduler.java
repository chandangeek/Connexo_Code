/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.ImmutableSet;

import javax.annotation.concurrent.GuardedBy;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wrapper around a ScheduledExecutorService that allows scheduling CronJobs according to their CronExpression.
 */
class CronExpressionScheduler {

    private final ScheduledExecutorService scheduledExecutorService;
    private final Clock clock;
    @GuardedBy("jobHandleLock")
    private HashMap<Long, ScheduledFuture<?>> scheduledJobHandles = new HashMap<>();
    private AtomicLong threadCounter = new AtomicLong(0);
    private final Object jobHandleLock = new Object();

    /**
     * Creates a new CronExpressionScheduler with the given size of thread pool.
     *
     * @param clock
     * @param threadPoolSize
     */
    public CronExpressionScheduler(Clock clock, int threadPoolSize) {
        this.clock = clock;
        scheduledExecutorService = Executors.newScheduledThreadPool(threadPoolSize, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("CronExpressionScheduler - " + threadCounter.incrementAndGet());
            return thread;
        });
    }

    /**
     * Schedules the given CronJob to execute once, at the next time its CronExpression matches.
     *
     * @param cronJob
     */
    public void submitOnce(CronJob cronJob) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        cronJob.getSchedule()
                .nextOccurrence(now)
                .ifPresent(
                        nextOccurrence -> {
                            long delay = Math.max(0L, nextOccurrence.toInstant().toEpochMilli() - now.toInstant().toEpochMilli());
                            synchronized (jobHandleLock) {
                                scheduledJobHandles.put(cronJob.getId(), scheduledExecutorService.schedule(cronJob, delay, TimeUnit.MILLISECONDS));
                            }
                        });
    }

    /**
     * Schedules the given CronJob to execute every time its CronExpression matches.
     *
     * @param cronJob
     */
    public void submit(CronJob cronJob) {
        submitOnce(new SelfReschedulingCronJob(cronJob));
    }

    public void unschedule(Long cronJobId, boolean mayInterruptIfRunning) {
        synchronized (jobHandleLock) {
            if (scheduledJobHandles.containsKey(cronJobId)) {
                scheduledJobHandles.remove(cronJobId).cancel(mayInterruptIfRunning);
            }
        }
    }

    public void unscheduleAll(boolean mayInterruptIfRunning) {
        synchronized (jobHandleLock) {
            ImmutableSet.copyOf(scheduledJobHandles.keySet())
                    .forEach(id -> unschedule(id, mayInterruptIfRunning));
        }
    }

    private final class SelfReschedulingCronJob implements CronJob {

        private final CronJob wrapped;

        private SelfReschedulingCronJob(CronJob wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public Long getId() {
            return wrapped.getId();
        }

        @Override
        public ScheduleExpression getSchedule() {
            return wrapped.getSchedule();
        }

        @Override
        public void run() {
            wrapped.run();
            synchronized (jobHandleLock) {
                if (scheduledJobHandles.containsKey(wrapped.getId())) {
                    submit(wrapped);
                }
            }
        }
    }

    /**
     * @see ScheduledExecutorService
     */
    public List<Runnable> shutdownNow() {
        return scheduledExecutorService.shutdownNow();
    }

    /**
     * @see ScheduledExecutorService
     */
    public void shutdown() {
        scheduledExecutorService.shutdown();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return scheduledExecutorService.awaitTermination(timeout, unit);
    }
}
