package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.InvalidValueException;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BoundedBigDecimalPropertyTest extends AbstractPropertySpecTest {

    @Test
    public void testLowerLimit() throws Exception {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl("test", BigDecimal.ONE, null);
        assertThat(test.validateValue(BigDecimal.valueOf(10L))).isTrue();
        assertThat(test.validateValue(BigDecimal.valueOf(1L))).isTrue();
        assertThat(test.validateValue(BigDecimal.valueOf(Long.MAX_VALUE))).isTrue();
    }

    @Test(expected = InvalidValueException.class)
    public void testLowerLimitExceeded() throws Exception {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl("test", BigDecimal.ONE, null);
        assertThat(test.validateValue(BigDecimal.valueOf(0L))).isFalse();
    }
    @Test
    public void testUpperLimit() throws Exception {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl("test", null, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(10L))).isTrue();
        assertThat(test.validateValue(BigDecimal.ONE)).isTrue();
    }

    @Test(expected = InvalidValueException.class)
    public void testUpperLimitExceeded() throws Exception {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl("test", null, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(11L))).isFalse();
    }

    @Test(expected = InvalidValueException.class)
    public void testUpperLimitExceededHigh() throws Exception {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl("test", null, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(Long.MAX_VALUE))).isFalse();
    }

    @Test
    public void testDoubleLimit() throws Exception {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl("test", BigDecimal.ONE, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(10L))).isTrue();
        assertThat(test.validateValue(BigDecimal.ONE)).isTrue();
        assertThat(test.validateValue(BigDecimal.valueOf(5L))).isTrue();
    }

    @Test(expected = InvalidValueException.class)
    public void testDoubleLimitExceededUnder() throws Exception {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl("test", BigDecimal.ONE, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(0L))).isFalse();
    }

    @Test(expected = InvalidValueException.class)
    public void testDoubleLimitExceededOver() throws Exception {
        BoundedBigDecimalPropertySpecImpl test = new BoundedBigDecimalPropertySpecImpl("test", BigDecimal.ONE, BigDecimal.TEN);
        assertThat(test.validateValue(BigDecimal.valueOf(11L))).isFalse();
    }
}
