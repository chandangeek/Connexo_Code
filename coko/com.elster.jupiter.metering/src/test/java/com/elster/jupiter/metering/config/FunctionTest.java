/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link Function} component.
 */
public class FunctionTest {

    @Test
    public void minInExpertMode() {
        assertThat(Function.MIN.supportedBy(Formula.Mode.EXPERT)).isTrue();
    }

    @Test
    public void maxInExpertMode() {
        assertThat(Function.MAX.supportedBy(Formula.Mode.EXPERT)).isTrue();
    }

    @Test
    public void minAggregationInExpertMode() {
        assertThat(Function.MIN_AGG.supportedBy(Formula.Mode.EXPERT)).isTrue();
    }

    @Test
    public void maxAggregationInExpertMode() {
        assertThat(Function.MAX_AGG.supportedBy(Formula.Mode.EXPERT)).isTrue();
    }

    @Test
    public void sumInExpertMode() {
        assertThat(Function.SUM.supportedBy(Formula.Mode.EXPERT)).isTrue();
    }

    @Test
    public void avergageInExpertMode() {
        assertThat(Function.AVG.supportedBy(Formula.Mode.EXPERT)).isTrue();
    }

    @Test
    public void timeBasedAggregationInExpertMode() {
        assertThat(Function.AGG_TIME.supportedBy(Formula.Mode.EXPERT)).isTrue();
    }

    @Test
    public void firstNotNullInExpertMode() {
        assertThat(Function.FIRST_NOT_NULL.supportedBy(Formula.Mode.EXPERT)).isTrue();
    }

    @Test
    public void minInAutoMode() {
        assertThat(Function.MIN.supportedBy(Formula.Mode.AUTO)).isTrue();
    }

    @Test
    public void maxInAutoMode() {
        assertThat(Function.MAX.supportedBy(Formula.Mode.AUTO)).isTrue();
    }

    @Test
    public void minAggregationInAutoMode() {
        assertThat(Function.MIN_AGG.supportedBy(Formula.Mode.AUTO)).isFalse();
    }

    @Test
    public void maxAggregationInAutoMode() {
        assertThat(Function.MAX_AGG.supportedBy(Formula.Mode.AUTO)).isFalse();
    }

    @Test
    public void sumInAutoMode() {
        assertThat(Function.SUM.supportedBy(Formula.Mode.AUTO)).isFalse();
    }

    @Test
    public void avergageInAutoMode() {
        assertThat(Function.AVG.supportedBy(Formula.Mode.AUTO)).isFalse();
    }

    @Test
    public void timeBasedAggregationInAutoMode() {
        assertThat(Function.AGG_TIME.supportedBy(Formula.Mode.AUTO)).isFalse();
    }

    @Test
    public void firstNotNullInAutoMode() {
        assertThat(Function.FIRST_NOT_NULL.supportedBy(Formula.Mode.AUTO)).isTrue();
    }

}