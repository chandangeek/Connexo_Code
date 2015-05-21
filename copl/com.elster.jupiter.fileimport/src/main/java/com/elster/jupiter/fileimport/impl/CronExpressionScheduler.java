package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around a ScheduledExecutorService that allows scheduling CronJobs according to their CronExpression.
 */
class CronExpressionScheduler {

    private final ScheduledExecutorService scheduledExecutorService;
    private final Clock clock;

    /**
     * Creates a new CronExpressionScheduler with the given size of thread pool.
     * @param clock
     * @param threadPoolSize
     */
    public CronExpressionScheduler(Clock clock, int threadPoolSize) {
        this.clock = clock;
        scheduledExecutorService = Executors.newScheduledThreadPool(threadPoolSize);
    }

    /**
     * Schedules the given CronJob to execute once, at the next time its CronExpression matches.
     * @param cronJob
     */
    public void submitOnce(CronJob cronJob) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        cronJob.getSchedule()
                .nextOccurrence(now)
                .ifPresent(
                        no -> {
                            long delay = no.toEpochSecond() * 1000 - now.toInstant().toEpochMilli();
                            scheduledExecutorService.schedule(cronJob, delay, TimeUnit.MILLISECONDS);
                        });
    }

    /**
     * Schedules the given CronJob to execute every time its CronExpression matches.
     * @param cronJob
     */
    public void submit(CronJob cronJob) {
        submitOnce(new SelfReschedulingCronJob(cronJob));
    }

    private final class SelfReschedulingCronJob implements CronJob {

        private final CronJob wrapped;

        private SelfReschedulingCronJob(CronJob wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public ScheduleExpression getSchedule() {
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
