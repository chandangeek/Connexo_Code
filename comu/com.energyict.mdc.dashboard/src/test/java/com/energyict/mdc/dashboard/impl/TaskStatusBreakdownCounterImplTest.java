/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link TaskStatusBreakdownCounterImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (13:26)
 */
public class TaskStatusBreakdownCounterImplTest {

    private static final long SUCCESS_COUNT = 12L;
    private static final long FAILED_COUNT = 45L;
    private static final long PENDING_COUNT = 78L;

    @Test
    public void testConstructorWithTargetOnly () {
        BreakdownTarget target = new BreakdownTarget();

        // Business method
        TaskStatusBreakdownCounterImpl<BreakdownTarget> breakdownCounter = new TaskStatusBreakdownCounterImpl<>(target);

        // Asserts
        assertThat(breakdownCounter.getCountTarget()).isEqualTo(target);
        assertThat(breakdownCounter.getSuccessCount()).isZero();
        assertThat(breakdownCounter.getFailedCount()).isZero();
        assertThat(breakdownCounter.getPendingCount()).isZero();
        assertThat(breakdownCounter.getCount()).isZero();
    }

    @Test
    public void testConstructorWithTargetAndValues () {
        BreakdownTarget target = new BreakdownTarget();

        // Business method
        TaskStatusBreakdownCounterImpl<BreakdownTarget> breakdownCounter = new TaskStatusBreakdownCounterImpl<>(target, SUCCESS_COUNT, FAILED_COUNT, PENDING_COUNT);

        // Asserts
        assertThat(breakdownCounter.getCountTarget()).isEqualTo(target);
        assertThat(breakdownCounter.getSuccessCount()).isEqualTo(SUCCESS_COUNT);
        assertThat(breakdownCounter.getFailedCount()).isEqualTo(FAILED_COUNT);
        assertThat(breakdownCounter.getPendingCount()).isEqualTo(PENDING_COUNT);
        assertThat(breakdownCounter.getCount()).isEqualTo(SUCCESS_COUNT + FAILED_COUNT + PENDING_COUNT);
    }

    @Test
    public void testSetValuesWithoutInitialValues () {
        BreakdownTarget target = new BreakdownTarget();
        TaskStatusBreakdownCounterImpl<BreakdownTarget> breakdownCounter = new TaskStatusBreakdownCounterImpl<>(target);

        // Business method
        breakdownCounter.setSuccessCount(SUCCESS_COUNT);
        breakdownCounter.setFailedCount(FAILED_COUNT);
        breakdownCounter.setPendingCount(PENDING_COUNT);

        // Asserts
        assertThat(breakdownCounter.getCountTarget()).isEqualTo(target);
        assertThat(breakdownCounter.getSuccessCount()).isEqualTo(SUCCESS_COUNT);
        assertThat(breakdownCounter.getFailedCount()).isEqualTo(FAILED_COUNT);
        assertThat(breakdownCounter.getPendingCount()).isEqualTo(PENDING_COUNT);
        assertThat(breakdownCounter.getCount()).isEqualTo(SUCCESS_COUNT + FAILED_COUNT + PENDING_COUNT);
    }

    @Test
    public void testSetValuesWithInitialValues () {
        BreakdownTarget target = new BreakdownTarget();
        TaskStatusBreakdownCounterImpl<BreakdownTarget> breakdownCounter = new TaskStatusBreakdownCounterImpl<>(target, SUCCESS_COUNT, FAILED_COUNT, PENDING_COUNT);

        // Business method
        breakdownCounter.setSuccessCount(0L);
        breakdownCounter.setFailedCount(0L);
        breakdownCounter.setPendingCount(0L);

        // Asserts
        assertThat(breakdownCounter.getCountTarget()).isEqualTo(target);
        assertThat(breakdownCounter.getSuccessCount()).isZero();
        assertThat(breakdownCounter.getFailedCount()).isZero();
        assertThat(breakdownCounter.getPendingCount()).isZero();
        assertThat(breakdownCounter.getCount()).isZero();
    }

    private class BreakdownTarget {}

}