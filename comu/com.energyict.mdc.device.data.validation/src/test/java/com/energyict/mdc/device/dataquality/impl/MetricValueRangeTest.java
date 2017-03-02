/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetricValueRangeTest {

    private static final String EXPRESSION = "fieldName";

    private SqlBuilder sqlBuilder;

    @Before
    public void setUp() {
        this.sqlBuilder = new SqlBuilder();
    }

    @Test
    public void ignoreMatch() {
        MetricValueRange.IgnoreRange ignoreRange = new MetricValueRange.IgnoreRange();

        // Business method
        ignoreRange.appendHavingTo(sqlBuilder, EXPRESSION);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(EXPRESSION + " >= 0");
    }

    @Test
    public void exactMatch() {
        MetricValueRange.ExactMatch exactMatch = new MetricValueRange.ExactMatch(10L);

        // Business method
        exactMatch.appendHavingTo(sqlBuilder, EXPRESSION);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(EXPRESSION + " = ? ");
    }

    @Test
    public void longMatchWithOpenRange() {
        MetricValueRange.LongRange longRange = new MetricValueRange.LongRange(Range.open(1L, 2L));

        // Business method
        longRange.appendHavingTo(sqlBuilder, EXPRESSION);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(EXPRESSION + " > ? and " + EXPRESSION + " < ? ");
    }

    @Test
    public void longMatchWithOpenClosedRange() {
        MetricValueRange.LongRange longRange = new MetricValueRange.LongRange(Range.openClosed(1L, 2L));

        // Business method
        longRange.appendHavingTo(sqlBuilder, EXPRESSION);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(EXPRESSION + " > ? and " + EXPRESSION + " <= ? ");
    }

    @Test
    public void longMatchWithClosedOpenRange() {
        MetricValueRange.LongRange longRange = new MetricValueRange.LongRange(Range.closedOpen(1L, 2L));

        // Business method
        longRange.appendHavingTo(sqlBuilder, EXPRESSION);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(EXPRESSION + " >= ? and " + EXPRESSION + " < ? ");
    }

    @Test
    public void longMatchWithClosedRange() {
        MetricValueRange.LongRange longRange = new MetricValueRange.LongRange(Range.closed(1L, 2L));

        // Business method
        longRange.appendHavingTo(sqlBuilder, EXPRESSION);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(EXPRESSION + " >= ? and " + EXPRESSION + " <= ? ");
    }

}
