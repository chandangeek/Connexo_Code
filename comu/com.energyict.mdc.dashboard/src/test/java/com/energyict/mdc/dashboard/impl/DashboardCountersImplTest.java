/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.Counter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DashboardCountersImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (10:40)
 */
public class DashboardCountersImplTest {

    private CounterTargets counters;

    @Test
    public void testConstructorWithNoCounters () {
        // Business method
        this.counters = new CounterTargets();

        // Asserts
        assertThat(this.counters.iterator().hasNext()).isFalse();
    }

    @Test
    public void testConstructorWithCounters () {
        Counter<CounterTarget> zero = new CounterImpl<>(new CounterTarget(1));
        Counter<CounterTarget> ten = new CounterImpl<>(new CounterTarget(2), 10);

        // Business method
        this.counters = new CounterTargets(zero, ten);

        // Asserts
        assertThat(this.counters.iterator().hasNext()).isTrue();
    }

    @Test
    public void testGetTotalWithoutCounters() {
        this.counters = new CounterTargets();

        // Business method
        long totalCount = this.counters.getTotalCount();

        // Asserts
        assertThat(totalCount).isZero();
    }

    @Test
    public void testGetTotalAfterAddingCounter() {
        this.counters = new CounterTargets();
        Counter<CounterTarget> ten = new CounterImpl<>(new CounterTarget(2), 10);
        this.counters.add(ten);

        // Business method
        long totalCount = this.counters.getTotalCount();

        // Asserts
        assertThat(totalCount).isEqualTo(10L);
    }

    @Test
    public void testGetTotalWithCounters() {
        Counter<CounterTarget> zero = new CounterImpl<>(new CounterTarget(1));
        Counter<CounterTarget> ten = new CounterImpl<>(new CounterTarget(2), 10);
        Counter<CounterTarget> hundred = new CounterImpl<>(new CounterTarget(3), 100);
        this.counters = new CounterTargets(zero, ten, hundred);

        // Business method
        long totalCount = this.counters.getTotalCount();

        // Asserts
        assertThat(totalCount).isEqualTo(110L);
    }

    private class CounterTargets extends DashboardCountersImpl<CounterTarget> {
        private CounterTargets() {
            super();
        }

        private CounterTargets(Counter<CounterTarget>... counters) {
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