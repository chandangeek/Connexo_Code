/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.tools;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.tools.Counter} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-30 (16:11)
 */
public class CounterTest {

    @Test
    public void testConstructor () {
        Counter counter = new Counter();
        assertThat(counter.getValue()).isZero();
    }

    @Test
    public void testResetOnInitial () {
        Counter counter = new Counter();
        counter.reset();
        assertThat(counter.getValue()).isZero();
    }

    @Test
    public void testResetOnIncrementedCounter () {
        Counter counter = new Counter();
        counter.increment();
        counter.increment();
        counter.increment();
        counter.increment();
        counter.reset();
        assertThat(counter.getValue()).isZero();
    }

    @Test
    public void testIncrement () {
        Counter counter = new Counter();
        counter.increment();
        assertThat(counter.getValue()).isEqualTo(1);
    }

    @Test
    public void testIncrementMultipleTimes () {
        Counter counter = new Counter();
        counter.increment();
        counter.increment();
        counter.increment();
        counter.increment();
        assertThat(counter.getValue()).isEqualTo(4);
    }

    @Test
    public void testIncrementAdd () {
        Counter counter = new Counter();
        counter.add(4);
        assertThat(counter.getValue()).isEqualTo(4);
    }

}