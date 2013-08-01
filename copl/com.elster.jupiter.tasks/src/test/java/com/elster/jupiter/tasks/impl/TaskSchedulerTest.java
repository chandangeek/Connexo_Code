package com.elster.jupiter.tasks.impl;

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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TaskSchedulerTest {

    private TaskScheduler taskScheduler;

    @Mock
    private TaskOccurrenceLauncher launcher;

    @Mock
    private CountDownLatch latch;

    @Before
    public void setUp() {
        latch = new CountDownLatch(3);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                latch.countDown();
                return null;
            }
        }).when(launcher).run();

        taskScheduler = new TaskScheduler(launcher, 10, TimeUnit.MILLISECONDS);
    }

    @After
    public void tearDown() {

    }

    @Test(timeout = 1000)
    public void test() throws InterruptedException {

        Thread testThread = new Thread(taskScheduler);
        testThread.start();

        boolean onTime = latch.await(50, TimeUnit.MILLISECONDS);

        verify(launcher, atLeast(3)).run();
        assertThat(onTime).isTrue();

        testThread.interrupt();

        assertThat(testThread.getState() == Thread.State.TERMINATED);
    }

}
