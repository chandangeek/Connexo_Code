/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time.impl;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ExecutionStatisticsImpl} component.
 */
public class ExecutionStatisticsImplTest {

    @Test
    public void initialZeroValues() {
        ExecutionStatisticsImpl statistics = this.getTestInstance();

        //Asserts
        assertThat(statistics.getCompleteCount()).isZero();
        assertThat(statistics.getTimeoutCount()).isZero();
        assertThat(statistics.getTotalExecutionTime()).isZero();
    }

    @Test
    public void getAverageDoesNotProduceArithmeticExceptionWhenCountIsZero() {
        ExecutionStatisticsImpl statistics = this.getTestInstance();

        // Business method
        statistics.getAverageExecutionTime();

        //Asserts: should not get ArithmeticException
    }

    @Test
    public void executionCompleted() {
        ExecutionStatisticsImpl statistics = this.getTestInstance();

        // Business method
        statistics.registerExecution(1000L);

        //Asserts
        assertThat(statistics.getCompleteCount()).isEqualTo(1L);
        assertThat(statistics.getTimeoutCount()).isZero();
        assertThat(statistics.getTotalExecutionTime()).isEqualTo(1000L);
        assertThat(statistics.getAverageExecutionTime()).isEqualTo(1000L);
        assertThat(statistics.getMinimumExecutionTime()).isEqualTo(1000L);
        assertThat(statistics.getMaximumExecutionTime()).isEqualTo(1000L);
    }

    @Test
    public void multipleCompleteExecutions() {
        ExecutionStatisticsImpl statistics = this.getTestInstance();

        // Business method
        statistics.registerExecution(1000L);
        statistics.registerExecution(2000L);
        statistics.registerExecution(9000L);

        //Asserts
        assertThat(statistics.getCompleteCount()).isEqualTo(3L);
        assertThat(statistics.getTimeoutCount()).isZero();
        assertThat(statistics.getTotalExecutionTime()).isEqualTo(12000L);
        assertThat(statistics.getAverageExecutionTime()).isEqualTo(4000L);
        assertThat(statistics.getMinimumExecutionTime()).isEqualTo(1000L);
        assertThat(statistics.getMaximumExecutionTime()).isEqualTo(9000L);
    }

    @Test
    public void mixedCompleteAndTimeoutExecutions() {
        ExecutionStatisticsImpl statistics = this.getTestInstance();

        // Business method
        statistics.registerTimeout();
        statistics.registerExecution(1000L);
        statistics.registerExecution(2000L);
        statistics.registerTimeout();
        statistics.registerExecution(9000L);

        //Asserts
        assertThat(statistics.getCompleteCount()).isEqualTo(3L);
        assertThat(statistics.getTimeoutCount()).isEqualTo(2L);
        assertThat(statistics.getTotalExecutionTime()).isEqualTo(12000L);
        assertThat(statistics.getAverageExecutionTime()).isEqualTo(4000L);
        assertThat(statistics.getMinimumExecutionTime()).isEqualTo(1000L);
        assertThat(statistics.getMaximumExecutionTime()).isEqualTo(9000L);
    }

    @Test
    public void executionTimedOut() {
        ExecutionStatisticsImpl statistics = this.getTestInstance();

        // Business method
        statistics.registerTimeout();

        //Asserts
        assertThat(statistics.getCompleteCount()).isZero();
        assertThat(statistics.getTimeoutCount()).isEqualTo(1L);
        assertThat(statistics.getTotalExecutionTime()).isZero();
    }

    @Test
    public void multipleTimeOuts() {
        ExecutionStatisticsImpl statistics = this.getTestInstance();

        // Business method
        statistics.registerTimeout();
        statistics.registerTimeout();
        statistics.registerTimeout();

        //Asserts
        assertThat(statistics.getCompleteCount()).isZero();
        assertThat(statistics.getTimeoutCount()).isEqualTo(3L);
        assertThat(statistics.getTotalExecutionTime()).isZero();
    }

    @Test
    public void reset() {
        ExecutionStatisticsImpl statistics = this.getTestInstance();
        statistics.registerTimeout();
        statistics.registerExecution(1000L);
        statistics.registerExecution(2000L);
        statistics.registerTimeout();
        statistics.registerExecution(9000L);

        // Business method
        statistics.reset();

        //Asserts
        assertThat(statistics.getCompleteCount()).isZero();
        assertThat(statistics.getTimeoutCount()).isZero();
        assertThat(statistics.getTotalExecutionTime()).isZero();
    }

    @Test
    public void getAverageDoesNotProduceArithmeticExceptionAfterReset() {
        ExecutionStatisticsImpl statistics = this.getTestInstance();
        statistics.registerTimeout();
        statistics.registerExecution(1000L);
        statistics.registerExecution(2000L);
        statistics.registerTimeout();
        statistics.registerExecution(9000L);
        statistics.reset();

        // Business method
        statistics.getAverageExecutionTime();

        //Asserts: should not get ArithmeticException
    }

    private ExecutionStatisticsImpl getTestInstance() {
        return new ExecutionStatisticsImpl();
    }

}