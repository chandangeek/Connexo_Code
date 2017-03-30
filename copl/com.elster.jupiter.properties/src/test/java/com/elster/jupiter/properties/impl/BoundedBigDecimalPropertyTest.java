/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.InvalidValueException;

import java.math.BigDecimal;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BoundedBigDecimalPropertyTest {

    @Test
    public void testLowerLimit() throws InvalidValueException {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl(BigDecimal.ONE, null);
        assertThat(test.validateValue(BigDecimal.valueOf(10L))).isTrue();
        assertThat(test.validateValue(BigDecimal.valueOf(1L))).isTrue();
        assertThat(test.validateValue(BigDecimal.valueOf(Long.MAX_VALUE))).isTrue();
    }

    @Test(expected = InvalidValueException.class)
    public void testLowerLimitExceeded() throws InvalidValueException {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl(BigDecimal.ONE, null);
        assertThat(test.validateValue(BigDecimal.valueOf(0L))).isFalse();
    }
    @Test
    public void testUpperLimit() throws InvalidValueException {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl(null, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(10L))).isTrue();
        assertThat(test.validateValue(BigDecimal.ONE)).isTrue();
    }

    @Test(expected = InvalidValueException.class)
    public void testUpperLimitExceeded() throws InvalidValueException {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl(null, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(11L))).isFalse();
    }

    @Test(expected = InvalidValueException.class)
    public void testUpperLimitExceededHigh() throws InvalidValueException {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl(null, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(Long.MAX_VALUE))).isFalse();
    }

    @Test
    public void testDoubleLimit() throws InvalidValueException {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl(BigDecimal.ONE, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(10L))).isTrue();
        assertThat(test.validateValue(BigDecimal.ONE)).isTrue();
        assertThat(test.validateValue(BigDecimal.valueOf(5L))).isTrue();
    }

    @Test(expected = InvalidValueException.class)
    public void testDoubleLimitExceededUnder() throws InvalidValueException {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl(BigDecimal.ONE, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(0L))).isFalse();
    }

    @Test(expected = InvalidValueException.class)
    public void testDoubleLimitExceededOver() throws InvalidValueException {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl(BigDecimal.ONE, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(11L))).isFalse();
    }

}