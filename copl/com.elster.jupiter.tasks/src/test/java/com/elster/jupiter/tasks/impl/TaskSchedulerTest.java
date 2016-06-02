package com.elster.jupiter.tasks.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskSchedulerTest {

    private TaskScheduler taskScheduler;

    private TaskOccurrenceLauncher launcher;

    private CountDownLatch latch;

    private LongAdder invocationCounter;


    @Before
    public void setUp() {
        latch = new CountDownLatch(3);
        invocationCounter = new LongAdder();

        launcher = () -> {
            synchronized (this) {
                invocationCounter.increment();
                latch.countDown();
            }
        };
        taskScheduler = new TaskScheduler(launcher, 10, TimeUnit.MILLISECONDS);
    }

    @After
    public void tearDown() {

    }

    @Test(timeout = 10000)
    public void testScheduling() throws InterruptedException {

        CountDownLatch threadStartedLatch = new CountDownLatch(1);
        Thread testThread = new Thread(() -> {
            threadStartedLatch.countDown();
            taskScheduler.run();
        });
        testThread.start();
        threadStartedLatch.await();

        boolean onTime = latch.await(5000, TimeUnit.MILLISECONDS);

        synchronized (launcher) {
            assertThat(invocationCounter.intValue()).isGreaterThanOrEqualTo(3);
        }
        assertThat(onTime).isTrue();

        testThread.interrupt();

        assertThat(testThread.getState() == Thread.State.TERMINATED);
    }

    @Ignore // use to evalute stability of test()
    @Test
    public void testLoop() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            if (i % 20 == 0) {
                System.out.println(i);
            }
            testScheduling();
            MockitoAnnotations.initMocks(this);
            setUp();
        }
    }

}
