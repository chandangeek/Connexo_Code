/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.time.ScheduleExpression;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CronExpressionSchedulerTest {

    private CronExpressionScheduler scheduler;
    private static final ZonedDateTime NOW = ZonedDateTime.ofInstant(Instant.ofEpochMilli(10), ZoneId.systemDefault()),
            FIRST_OCCURRENCE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(20), ZoneId.systemDefault()),
            SECOND_OCCURRENCE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(30), ZoneId.systemDefault());
    @Mock
    private CronJob cronJob;
    @Mock
    private ScheduleExpression scheduleExpression;
    @Mock
    private Clock clock;
    @Mock
    private Clock zonedTime;

    @Before
    public void setUp() {
        scheduler = new CronExpressionScheduler(clock, 1);
        when(cronJob.getSchedule()).thenReturn(scheduleExpression);
        when(cronJob.getId()).thenReturn(45L);
        when(scheduleExpression.nextOccurrence(NOW)).thenReturn(Optional.of(FIRST_OCCURRENCE));
        when(scheduleExpression.nextOccurrence(FIRST_OCCURRENCE)).thenReturn(Optional.of(SECOND_OCCURRENCE));
        when(clock.instant()).thenReturn(NOW.toInstant(), FIRST_OCCURRENCE.toInstant(), SECOND_OCCURRENCE.toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @After
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    public void testSubmitOnce() throws InterruptedException {
        final CountDownLatch cronJobExecuted = new CountDownLatch(1);

        doAnswer(invocationOnMock -> {
            cronJobExecuted.countDown();
            return null;
        }).when(cronJob).run();

        scheduler.submitOnce(cronJob);

        cronJobExecuted.await(2, TimeUnit.SECONDS);

        verify(cronJob, times(1)).run();
    }

    @Test
    public void testSubmit() throws InterruptedException {
        final CountDownLatch cronJobExecuted = new CountDownLatch(2);

        doAnswer(invocationOnMock -> {
            cronJobExecuted.countDown();
            return null;
        }).when(cronJob).run();

        scheduler.submit(cronJob);

        cronJobExecuted.await(2, TimeUnit.SECONDS); // use without timeout for debugging!

        verify(cronJob, times(2)).run();
    }

    @Test
    public void testUnschedule() throws InterruptedException {
        final CountDownLatch cronJobStarted = new CountDownLatch(1);
        final CountDownLatch cronJobFinish = new CountDownLatch(1);
        final AtomicLong counter = new AtomicLong();

        doAnswer(invocationOnMock -> {
            try {
                counter.incrementAndGet();
                cronJobStarted.countDown();
                cronJobFinish.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }).when(cronJob).run();
        when(scheduleExpression.nextOccurrence(any())).thenAnswer(invocation -> Optional.of((ZonedDateTime) invocation.getArguments()[0]));

        scheduler.submit(cronJob);

        cronJobStarted.await();

        scheduler.unschedule(cronJob.getId(), true);

        Thread.sleep(50);

        assertThat(counter.get()).isEqualTo(1);

    }

}
