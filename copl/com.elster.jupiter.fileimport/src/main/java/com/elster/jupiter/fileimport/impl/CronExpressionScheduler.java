package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.cron.CronExpression;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CronExpressionScheduler {

    private final ScheduledExecutorService scheduledExecutorService;

    public CronExpressionScheduler(int threadPoolSize) {
        scheduledExecutorService = Executors.newScheduledThreadPool(threadPoolSize);
    }

    public void submitOnce(CronJob cronJob) {
        Date next = cronJob.getSchedule().nextAfter(Bus.getClock().now());
        if (next != null) {
            long delay = next.getTime() - Bus.getClock().now().getTime();
            scheduledExecutorService.schedule(cronJob, delay, TimeUnit.MILLISECONDS);
        }
    }

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

    public List<Runnable> shutdownNow() {
        return scheduledExecutorService.shutdownNow();
    }

    public void shutdown() {
        scheduledExecutorService.shutdown();
    }
}
