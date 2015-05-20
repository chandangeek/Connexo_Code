package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.cron.CronExpression;

import java.time.Clock;
import java.time.Instant;

import com.elster.jupiter.util.time.ScheduleExpression;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CronExpressionSchedulerTest {

    private CronExpressionScheduler scheduler;
    private static final ZonedDateTime now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(10),ZoneId.systemDefault()),
            firstOccurrence = ZonedDateTime.ofInstant(Instant.ofEpochMilli(20),ZoneId.systemDefault()),
            secondOccurrence = ZonedDateTime.ofInstant(Instant.ofEpochMilli(30), ZoneId.systemDefault());
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
        when(scheduleExpression.nextOccurrence(now)).thenReturn(Optional.of(firstOccurrence));
        when(scheduleExpression.nextOccurrence(firstOccurrence)).thenReturn(Optional.of(secondOccurrence));
        when(clock.instant()).thenReturn(now.toInstant(), firstOccurrence.toInstant(), secondOccurrence.toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @After
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    public void testSubmitOnce() throws InterruptedException {
        final CountDownLatch cronJobExecuted = new CountDownLatch(1);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                cronJobExecuted.countDown();
                return null;
            }
        }).when(cronJob).run();

        scheduler.submitOnce(cronJob);

        cronJobExecuted.await(2, TimeUnit.SECONDS);

        verify(cronJob, times(1)).run();
    }

    @Test
    public void testSubmit() throws InterruptedException {
        final CountDownLatch cronJobExecuted = new CountDownLatch(2);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                cronJobExecuted.countDown();
                return null;
            }
        }).when(cronJob).run();

        scheduler.submit(cronJob);

        cronJobExecuted.await(2, TimeUnit.SECONDS); // use without timeout for debugging!

        verify(cronJob, times(2)).run();
    }


}
