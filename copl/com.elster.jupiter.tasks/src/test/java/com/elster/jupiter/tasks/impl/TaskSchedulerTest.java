/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class TaskSchedulerTest {

    private static final long NANOS_PER_MILLI = 1_000_000L;

    @Test(timeout = 60_000)
    public void testScheduling() throws InterruptedException {

        Queue<Long> executionNanos = new ConcurrentLinkedQueue<>();
        CountDownLatch runs = new CountDownLatch(10);

        TaskOccurrenceLauncher launcher = () -> {
            executionNanos.add(System.nanoTime());
            runs.countDown();
        };
        TaskScheduler taskScheduler = new TaskScheduler(launcher, 1, TimeUnit.MILLISECONDS, factory -> Executors.newScheduledThreadPool(1, factory));
        Thread schedulerThread = new Thread(taskScheduler::run);
        schedulerThread.start();
        try {
            runs.await();
        } finally {
            schedulerThread.interrupt();
        }

        assertThat(executionNanos.size()).isGreaterThanOrEqualTo(10);
    }

    @Test
    public void testScheduleNow() throws InterruptedException {

        Queue<Long> executionNanos = new ConcurrentLinkedQueue<>();
        CountDownLatch initialRun = new CountDownLatch(1);
        CountDownLatch runNowRun = new CountDownLatch(2);

        TaskOccurrenceLauncher launcher = () -> {
            executionNanos.add(System.nanoTime());
            initialRun.countDown();
            runNowRun.countDown();
        };
        TaskScheduler taskScheduler = new TaskScheduler(launcher, 1, TimeUnit.DAYS, factory -> Executors.newScheduledThreadPool(1, factory));
        Thread schedulerThread = new Thread(taskScheduler::run);
        schedulerThread.start();
        try {
            boolean onTime = initialRun.await(1, TimeUnit.MINUTES);
            assertThat(onTime).isTrue();

            // call to test :
            taskScheduler.scheduleNow();

            onTime = runNowRun.await(1, TimeUnit.MINUTES);
            assertThat(onTime).isTrue();
        } finally {
            schedulerThread.interrupt();
        }

        assertThat(executionNanos.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void testShutdown() throws InterruptedException {
        Set<Long> tokens = LongStream.range(0, 100)
                .boxed()
                .collect(Collectors.toCollection(() -> Collections.newSetFromMap(new ConcurrentHashMap<>())));
        AtomicLong eat = new AtomicLong(0);
        AtomicBoolean runWhenInterruptedOnce = new AtomicBoolean(false);
        AtomicBoolean runWhenInterruptedTwice = new AtomicBoolean(false);
        CountDownLatch startedEating = new CountDownLatch(1);

        TaskOccurrenceLauncher launcher = () -> {
            if (Thread.currentThread().isInterrupted()) {
                if (runWhenInterruptedOnce.getAndSet(true)) {
                    runWhenInterruptedTwice.set(true);
                }
            }
            startedEating.countDown();
            tokens.remove(eat.getAndIncrement());
        };
        ScheduledExecutorService scheduledExecutorService = spy(Executors.newScheduledThreadPool(1));
        TaskScheduler taskScheduler = new TaskScheduler(launcher, 5, TimeUnit.MILLISECONDS, factory -> scheduledExecutorService);

        Thread schedulerThread = new Thread(taskScheduler::run);
        schedulerThread.start();
        try {
            boolean onTime = startedEating.await(1, TimeUnit.MINUTES);
            assertThat(onTime).isTrue();
        } finally {
            schedulerThread.interrupt();
        }

        await().atMost(1, TimeUnit.MINUTES).until(scheduledExecutorService::isShutdown);

        scheduledExecutorService.awaitTermination(60, TimeUnit.SECONDS);

        assertThat(runWhenInterruptedTwice.get()).isFalse();
    }


//    private TaskScheduler taskScheduler;
//    private TaskOccurrenceLauncher launcher;
//    private CountDownLatch latch;
//    private LongAdder invocationCounter;
//
//
//    @Before
//    public void setUp() {
//        latch = new CountDownLatch(3);
//        invocationCounter = new LongAdder();
//
//        launcher = () -> {
//            synchronized (this) {
//                invocationCounter.increment();
//                latch.countDown();
//            }
//        };
//        taskScheduler = new TaskScheduler(launcher, 10, TimeUnit.MILLISECONDS);
//    }
//
//    @After
//    public void tearDown() {
//
//    }
//
//    @Test(timeout = 10000)
//    public void testScheduling() throws InterruptedException {
//
//        CountDownLatch threadStartedLatch = new CountDownLatch(1);
//        Thread testThread = new Thread(() -> {
//            threadStartedLatch.countDown();
//            taskScheduler.run();
//        });
//        testThread.start();
//        threadStartedLatch.await();
//
//        boolean onTime = latch.await(5000, TimeUnit.MILLISECONDS);
//
//        synchronized (launcher) {
//            assertThat(invocationCounter.intValue()).isGreaterThanOrEqualTo(3);
//        }
//        assertThat(onTime).isTrue();
//
//        testThread.interrupt();
//
//        assertThat(testThread.getState() == Thread.State.TERMINATED);
//    }
//
////    @Ignore // use to evalute stability of test()
//    @Test
//    public void testLoop() throws InterruptedException {
//        for (int i = 0; i < 10000; i++) {
//            if (i % 20 == 0) {
//                System.out.println(i);
//            }
//            testScheduling();
//            MockitoAnnotations.initMocks(this);
//            setUp();
//        }
//    }
//
}
