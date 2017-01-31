/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.ExecutionStatistics;
import com.elster.jupiter.util.time.StopWatch;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.time.Duration;
import java.util.Dictionary;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the {@link ExecutionTimerImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-10 (17:21)
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutionTimerImplTest {

    private static final String TIMER_NAME = ExecutionTimerImplTest.class.getSimpleName();

    @Mock
    private BundleContext bundleContext;
    @Mock
    private ServiceRegistration serviceRegistration;
    @Mock
    private IExecutionTimerService executionTimerService;

    @Before
    public void initializeMocks() {
        when(this.bundleContext.registerService(any(Class.class), anyObject(), any(Dictionary.class))).thenReturn(this.serviceRegistration);
    }

    @Test
    public void testAttributes() {
        // Business method
        String expectedName = "testAttributes";
        long expectedTimeoutMillis = 951L;
        ExecutionTimerImpl timer = this.getTestInstance(expectedName, expectedTimeoutMillis);

        // Asserts
        assertThat(timer.getName()).isEqualTo(expectedName);
        assertThat(timer.getTimeout()).isEqualTo(Duration.ofMillis(expectedTimeoutMillis));
    }

    @Test
    public void activationRegistersStatisticsAsJmxBean() {
        // Business method
        ExecutionTimerImpl timer = this.getTestInstance();

        // Asserts
        ArgumentCaptor<Dictionary> dictionaryArgumentCaptor = ArgumentCaptor.forClass(Dictionary.class);
        verify(this.bundleContext).registerService(eq(ExecutionStatisticsImpl.class), any(ExecutionStatisticsImpl.class), dictionaryArgumentCaptor.capture());
        Dictionary dictionary = dictionaryArgumentCaptor.getValue();
        assertThat(dictionary).isNotNull();
        assertThat(dictionary.get("jmx.objectname")).isNotNull();
    }

    @Test
    public void deactivationUnregistersStatisticsAsJmxBean() {
        ExecutionTimerImpl timer = this.getTestInstance();

        // Business method
        timer.deactivate();

        // Asserts
        verify(this.serviceRegistration).unregister();
    }

    @Test
    public void runnableCompletes() {
        ExecutionTimerImpl timer = this.getTestInstance(TIMER_NAME, 100L);

        // Business method
        timer.time(new WaitingRunnable(2L));

        // Asserts
        ExecutionStatistics statistics = timer.getStatistics();
        assertThat(statistics).isNotNull();
        assertThat(statistics.getCompleteCount()).isEqualTo(1L);
        assertThat(statistics.getTimeoutCount()).isZero();
    }

    @Test
    public void runnableTimesOut() {
        ExecutionTimerImpl timer = this.getTestInstance(TIMER_NAME, 2L);

        // Business method
        timer.time(new WaitingRunnable(10L));

        // Asserts
        ExecutionStatistics statistics = timer.getStatistics();
        assertThat(statistics).isNotNull();
        assertThat(statistics.getCompleteCount()).isZero();
        assertThat(statistics.getTimeoutCount()).isEqualTo(1L);
    }

    @Test
    public void callableCompletes() throws Exception {
        ExecutionTimerImpl timer = this.getTestInstance(TIMER_NAME, 100L);

        // Business method
        timer.time(new WaitingCallable(2L, "whatever"));

        // Asserts
        ExecutionStatistics statistics = timer.getStatistics();
        assertThat(statistics).isNotNull();
        assertThat(statistics.getCompleteCount()).isEqualTo(1L);
        assertThat(statistics.getTimeoutCount()).isZero();
    }

    @Test
    public void callableTimesOut() throws Exception {
        ExecutionTimerImpl timer = this.getTestInstance(TIMER_NAME, 2L);

        // Business method
        timer.time(new WaitingCallable(10L, "whatever"));

        // Asserts
        ExecutionStatistics statistics = timer.getStatistics();
        assertThat(statistics).isNotNull();
        assertThat(statistics.getCompleteCount()).isZero();
        assertThat(statistics.getTimeoutCount()).isEqualTo(1L);
    }

    @Test
    public void callableResultWithoutTimeout() throws Exception {
        ExecutionTimerImpl timer = this.getTestInstance(TIMER_NAME, 100L);
        String expectedResult = "whatever";

        // Business method
        String result = timer.time(new WaitingCallable(1L, expectedResult));

        // Asserts
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void callableResultWithTimeout() throws Exception {
        ExecutionTimerImpl timer = this.getTestInstance(TIMER_NAME, 2L);
        String expectedResult = "whatever";

        // Business method
        String result = timer.time(new WaitingCallable(10L, expectedResult));

        // Asserts
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void reset() throws Exception {
        ExecutionTimerImpl timer = this.getTestInstance();
        timer.time(new WaitingCallable(10L, "whatever"));
        timer.time(new WaitingRunnable(1L));

        // Business method
        timer.reset();

        // Asserts
        ExecutionStatistics statistics = timer.getStatistics();
        assertThat(statistics).isNotNull();
        assertThat(statistics.getCompleteCount()).isZero();
        assertThat(statistics.getTimeoutCount()).isZero();
    }

    @Test
    public void joinWithTheSameTimeout() throws Exception {
        ExecutionTimerImpl parent = this.getTestInstance("parent", 50L);
        ExecutionTimerImpl child1 = this.getTestInstance("child1", 50L);
        ExecutionTimerImpl child2 = this.getTestInstance("child2", 50L);

        // Business method
        parent.join(child1);
        parent.join(child2);

        // Asserts
        child1.time(new WaitingRunnable(100L));  // timeout
        child2.time(new WaitingRunnable(5L));   // no timeout
        ExecutionStatistics statistics = parent.getStatistics();
        assertThat(statistics).isNotNull();
        assertThat(statistics.getCompleteCount()).isEqualTo(1L);
        assertThat(statistics.getTimeoutCount()).isEqualTo(1L);
    }

    @Test
    public void joinWithTheDifferentTimeout() throws Exception {
        ExecutionTimerImpl parent = this.getTestInstance("parent", 50L);
        ExecutionTimerImpl child1 = this.getTestInstance("child1", 100L);
        ExecutionTimerImpl child2 = this.getTestInstance("child2", 100L);

        // Business method
        parent.join(child1);
        parent.join(child2);

        // Asserts
        child1.time(new WaitingRunnable(60L));   // no timeout for child but timeout for parent
        child2.time(new WaitingRunnable(5L));    // no timeout
        ExecutionStatistics statistics = parent.getStatistics();
        assertThat(statistics).isNotNull();
        assertThat(statistics.getCompleteCount()).isEqualTo(1L);
        assertThat(statistics.getTimeoutCount()).isEqualTo(1L);
    }

    private ExecutionTimerImpl getTestInstance() {
        return this.getTestInstance(TIMER_NAME, 1);
    }

    private ExecutionTimerImpl getTestInstance(String name, long timeOutMillis) {
        ExecutionTimerImpl timer = new ExecutionTimerImpl(executionTimerService, name, Duration.ofMillis(timeOutMillis));
        timer.activate(this.bundleContext);
        return timer;
    }

    private class WaitingRunnable implements Runnable {
        private final long delayMillis;

        private WaitingRunnable(long delayMillis) {
            this.delayMillis = delayMillis;
        }

        @Override
        public void run() {
            StopWatch stopWatch = new StopWatch();
            while (stopWatch.getElapsed() < this.delayMillis * 1_000_000L) {
                // Pretend to do something
            }
        }
    }

    private class WaitingCallable implements Callable<String> {
        private final long delayMillis;
        private final String answer;

        private WaitingCallable(long delayMillis, String answer) {
            this.delayMillis = delayMillis;
            this.answer = answer;
        }

        @Override
        public String call() throws Exception {
            StopWatch stopWatch = new StopWatch();
            while (stopWatch.getElapsed() < this.delayMillis * 1_000_000L) {
                // Pretend to do something
            }
            return this.answer;
        }
    }

}