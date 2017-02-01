/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link Function} component.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2016-02-19 (11:23)
 */
public class FunctionTest {

    @Test
    public void min() {
        assertThat(Function.from(com.elster.jupiter.metering.config.Function.MIN_AGG)).isEqualTo(Function.MIN);
    }

    @Test
    public void max() {
        assertThat(Function.from(com.elster.jupiter.metering.config.Function.MAX_AGG)).isEqualTo(Function.MAX);
    }

    @Test
    public void least() {
        assertThat(Function.from(com.elster.jupiter.metering.config.Function.MIN)).isEqualTo(Function.LEAST);
    }

    @Test
    public void greatest() {
        assertThat(Function.from(com.elster.jupiter.metering.config.Function.MAX)).isEqualTo(Function.GREATEST);
    }

    @Test
    public void sum() {
        assertThat(Function.from(com.elster.jupiter.metering.config.Function.SUM)).isEqualTo(Function.SUM);
    }

    @Test
    public void avg() {
        assertThat(Function.from(com.elster.jupiter.metering.config.Function.AVG)).isEqualTo(Function.AVG);
    }

    @Test
    public void aggTime() {
        assertThat(Function.from(com.elster.jupiter.metering.config.Function.AGG_TIME)).isEqualTo(Function.AGG_TIME);
    }

    @Test
    public void coalesce() {
        assertThat(Function.from(com.elster.jupiter.metering.config.Function.FIRST_NOT_NULL)).isEqualTo(Function.COALESCE);
    }

}