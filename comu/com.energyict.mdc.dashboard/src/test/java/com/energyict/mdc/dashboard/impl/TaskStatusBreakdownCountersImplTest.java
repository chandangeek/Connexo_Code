/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link TaskStatusBreakdownCountersImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (13:12)
 */
public class TaskStatusBreakdownCountersImplTest {

    private static final long SUCCESS_COUNT = 12L;
    private static final long FAILED_COUNT = 45L;
    private static final long PENDING_COUNT = 78L;

    private Breakdown breakdown;

    @Test
    public void testConstructorWithNoBreakdowns() {
        // Business method
        this.breakdown = new Breakdown();

        // Asserts
        assertThat(this.breakdown.iterator().hasNext()).isFalse();
    }

    @Test
    public void testConstructorWithBreakdowns () {
        TaskStatusBreakdownCounter<CounterTarget> allZeros = new TaskStatusBreakdownCounterImpl<>(new CounterTarget(1));
        TaskStatusBreakdownCounter<CounterTarget> nonZeros = new TaskStatusBreakdownCounterImpl<>(new CounterTarget(2), SUCCESS_COUNT, FAILED_COUNT, PENDING_COUNT);

        // Business method
        this.breakdown = new Breakdown(allZeros, nonZeros);

        // Asserts
        assertThat(this.breakdown.iterator().hasNext()).isTrue();
    }

    @Test
    public void testGetTotalsWithoutBreakdowns() {
        this.breakdown = new Breakdown();

        // Business method
        long totalCount = this.breakdown.getTotalCount();
        long totalSuccessCount = this.breakdown.getTotalSuccessCount();
        long totalFailureCount = this.breakdown.getTotalFailedCount();
        long totalPendingCount = this.breakdown.getTotalPendingCount();

        // Asserts
        assertThat(totalSuccessCount).isZero();
        assertThat(totalFailureCount).isZero();
        assertThat(totalPendingCount).isZero();
        assertThat(totalCount).isZero();
    }

    @Test
    public void testGetTotalsAfterAddingBreakdown() {
        this.breakdown = new Breakdown();
        TaskStatusBreakdownCounter<CounterTarget> nonZeros = new TaskStatusBreakdownCounterImpl<>(new CounterTarget(2), SUCCESS_COUNT, FAILED_COUNT, PENDING_COUNT);
        this.breakdown.add(nonZeros);

        // Business method
        long totalCount = this.breakdown.getTotalCount();
        long totalSuccessCount = this.breakdown.getTotalSuccessCount();
        long totalFailureCount = this.breakdown.getTotalFailedCount();
        long totalPendingCount = this.breakdown.getTotalPendingCount();

        // Asserts
        assertThat(totalSuccessCount).isEqualTo(SUCCESS_COUNT);
        assertThat(totalFailureCount).isEqualTo(FAILED_COUNT);
        assertThat(totalPendingCount).isEqualTo(PENDING_COUNT);
        assertThat(totalCount).isEqualTo(SUCCESS_COUNT + FAILED_COUNT + PENDING_COUNT);
    }

    @Test
    public void testGetTotalsWithBreakdowns() {
        TaskStatusBreakdownCounter<CounterTarget> allZeros = new TaskStatusBreakdownCounterImpl<>(new CounterTarget(1));
        TaskStatusBreakdownCounter<CounterTarget> nonZeros = new TaskStatusBreakdownCounterImpl<>(new CounterTarget(2), SUCCESS_COUNT, FAILED_COUNT, PENDING_COUNT);
        this.breakdown = new Breakdown(allZeros, nonZeros);

        // Business method
        long totalCount = this.breakdown.getTotalCount();
        long totalSuccessCount = this.breakdown.getTotalSuccessCount();
        long totalFailureCount = this.breakdown.getTotalFailedCount();
        long totalPendingCount = this.breakdown.getTotalPendingCount();

        // Asserts
        assertThat(totalSuccessCount).isEqualTo(SUCCESS_COUNT);
        assertThat(totalFailureCount).isEqualTo(FAILED_COUNT);
        assertThat(totalPendingCount).isEqualTo(PENDING_COUNT);
        assertThat(totalCount).isEqualTo(SUCCESS_COUNT + FAILED_COUNT + PENDING_COUNT);
    }

    private class Breakdown extends TaskStatusBreakdownCountersImpl<CounterTarget> {
        private Breakdown() {
            super();
        }

        private Breakdown(TaskStatusBreakdownCounter<CounterTarget>... counters) {
            super(counters);
        }
    }

    private class CounterTarget {
        private final int id;

        private CounterTarget(int id) {
            super();
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CounterTarget that = (CounterTarget) o;

            if (id != that.id) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

}