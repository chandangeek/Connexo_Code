package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.cron.CronExpression;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around a ScheduledExecutorService that allows scheduling CronJobs according to their CronExpression.
 */
class CronExpressionScheduler {

    private final ScheduledExecutorService scheduledExecutorService;

    /**
     * Creates a new CronExpressionScheduler with the given size of thread pool.
     * @param threadPoolSize
     */
    public CronExpressionScheduler(int threadPoolSize) {
        scheduledExecutorService = Executors.newScheduledThreadPool(threadPoolSize);
    }

    /**
     * Schedules the given CronJob to execute once, at the next time its CronExpression matches.
     * @param cronJob
     */
    public void submitOnce(CronJob cronJob) {
        Date next = cronJob.getSchedule().nextAfter(Bus.getClock().now());
        if (next != null) {
            long delay = next.getTime() - Bus.getClock().now().getTime();
            scheduledExecutorService.schedule(cronJob, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Schedules the given CronJob to execute every time its CronExpression matches.
     * @param cronJob
     */
    public void submit(CronJob cronJob) {
        submitOnce(new SelfReschedulingCronJob(cronJob));
    }

    private class SelfReschedulingCronJob implements CronJob {

        private final CronJob wrapped;

        private SelfReschedulingCronJob(CronJob wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public CronExpression getSchedule() {
            return wrapped.getSchedule();
        }

        @Override
        public void run() {
            wrapped.run();
            submitOnce(this);
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
}
