package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.util.cron.CronExpression;

import java.time.Clock;
import java.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CronExpressionSchedulerTest {

    private CronExpressionScheduler scheduler;

    private static final Instant now = Instant.ofEpochMilli(10), firstOccurrence =Instant.ofEpochMilli(20), secondOccurrence = Instant.ofEpochMilli(30);

    @Mock
    private CronJob cronJob;
    @Mock
    private CronExpression cronExpression;
    @Mock
    private Clock clock;



    @Before
    public void setUp() {
        scheduler = new CronExpressionScheduler(clock, 1);

        when(cronJob.getSchedule()).thenReturn(cronExpression);
        when(cronExpression.nextAfter(now)).thenReturn(firstOccurrence);
        when(cronExpression.nextAfter(firstOccurrence)).thenReturn(secondOccurrence);
        when(clock.instant()).thenReturn(now, firstOccurrence, secondOccurrence);
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
