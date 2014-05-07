package com.energyict.mdc.engine.impl.tools;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.junit.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.energyict.mdc.engine.impl.tools.Equality.equalityHoldsFor;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.tools.Equality} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (11:52)
 */
public class EqualityTest {

    private static final Timestamp TIMESTAMP = new Timestamp(113, Calendar.MAY, 1, 10, 50, 23, 423156889);
    private static final Date DATE = new DateTime(2013, DateTimeConstants.MAY, 1, 10, 50, 23, 423, DateTimeZone.forTimeZone(TimeZone.getDefault())).toDate();

    @Test
    public void testFluentStringEqualityTrue() {
        assertThat(equalityHoldsFor("first").and("first")).isTrue();
    }

    @Test
    public void testFluentStringEqualityFalse() {
        assertThat(equalityHoldsFor("first").and("second")).isFalse();
    }

    @Test
    public void testFluentStringEqualitySecondArgumentIsNull() {
        assertThat(equalityHoldsFor("first").and(null)).isFalse();
    }

    @Test
    public void testFluentStringEqualityFirstArgumentIsNull() {
        assertThat(equalityHoldsFor(null).and("second")).isFalse();
    }

    @Test
    public void testFluentBigDecimalEqualityOnZero() {
        assertThat(equalityHoldsFor(BigDecimal.ZERO).and(BigDecimal.ZERO)).isTrue();
    }

    @Test
    public void testFluentBigDecimalEqualityTrivialCase() {
        assertThat(equalityHoldsFor(new BigDecimal("123.456")).and(new BigDecimal("123.456"))).isTrue();
    }

    @Test
    public void testFluentBigDecimalEqualityDifferentScalesButSameValueYieldsTrue() {
        assertThat(equalityHoldsFor(new BigDecimal("123.000")).and(new BigDecimal("123"))).isTrue();
    }

    @Test
    public void testFluentBigDecimalEqualityTriviallyFalse() {
        assertThat(equalityHoldsFor(BigDecimal.TEN).and(BigDecimal.ONE)).isFalse();
    }

    @Test
    public void testFluentBigDecimalEqualitySecondArgumentIsNull() {
        assertThat(equalityHoldsFor(BigDecimal.TEN).and(null)).isFalse();
    }

    @Test
    public void testFluentBigDecimalEqualityFirstArgumentIsNull() {
        assertThat(equalityHoldsFor(null).and(BigDecimal.TEN)).isFalse();
    }

    @Test
    public void testFluentBigDecimalEqualityOnZeroPassedAsObject() {
        assertThat(equalityHoldsFor((Object) BigDecimal.ZERO).and((Object) BigDecimal.ZERO)).isTrue();
    }

    @Test
    public void testFluentBigDecimalEqualityTrivialCasePassedAsObject() {
        assertThat(equalityHoldsFor((Object) new BigDecimal("123.456")).and((Object) new BigDecimal("123.456"))).isTrue();
    }

    @Test
    public void testFluentBigDecimalEqualityDifferentScalesButSameValueYieldsTruePassedAsObject() {
        assertThat(equalityHoldsFor((Object) new BigDecimal("123.000")).and((Object) new BigDecimal("123"))).isTrue();
    }

    @Test
    public void testFluentBigDecimalEqualityTriviallyFalsePassedAsObject() {
        assertThat(equalityHoldsFor((Object) BigDecimal.TEN).and((Object) BigDecimal.ONE)).isFalse();
    }

    @Test
    public void testFluentBigDecimalEqualitySecondArgumentIsNullPassedAsObject() {
        assertThat(equalityHoldsFor((Object) BigDecimal.TEN).and(null)).isFalse();
    }

    @Test
    public void testFluentBigDecimalEqualityFirstArgumentIsNullPassedAsObject() {
        assertThat(equalityHoldsFor(null).and((Object) BigDecimal.TEN)).isFalse();
    }

    @Test
    public void testFluentNullEqualsNull () {
        assertThat(equalityHoldsFor(null).and(null)).isTrue();
    }

    @Test
    public void testFluentDateEqualityTrivialCase() {
        assertThat(equalityHoldsFor(new Date(17L)).and(new Date(17L))).isTrue();
    }

    @Test
    public void testFluentDateEqualityBetweenTimeStampAndDate() {
        assertThat(equalityHoldsFor(TIMESTAMP).and(DATE)).isTrue();
    }

    @Test
    public void testFluentDateEqualityBetweenDateAndTimeStamp() {
        assertThat(equalityHoldsFor(DATE).and(TIMESTAMP)).isTrue();
    }

    @Test
    public void testFluentDateEqualityTriviallyFalse() {
        assertThat(equalityHoldsFor(new Date(18L)).and(new Date(17L))).isFalse();
    }

    @Test
    public void testFluentDateEqualitySecondArgumentIsNull() {
        assertThat(equalityHoldsFor(new Date(18L)).and(null)).isFalse();
    }

    @Test
    public void testFluentDateEqualityFirstArgumentIsNull() {
        assertThat(equalityHoldsFor(null).and(new Date(18L))).isFalse();
    }

    @Test
    public void testFluentDateEqualityTrivialCasePassedAsObject() {
        assertThat(equalityHoldsFor((Object) new Date(17L)).and((Object) new Date(17L))).isTrue();
    }

    @Test
    public void testFluentDateEqualityBetweenTimeStampAndDatePassedAsObject() {
        assertThat(equalityHoldsFor((Object) TIMESTAMP).and((Object) DATE)).isTrue();
    }

    @Test
    public void testFluentDateEqualityBetweenDateAndTimeStampPassedAsObject() {
        assertThat(equalityHoldsFor((Object) DATE).and((Object) TIMESTAMP)).isTrue();
    }

    @Test
    public void testFluentDateEqualityTriviallyFalsePassedAsObject() {
        assertThat(equalityHoldsFor((Object) new Date(18L)).and((Object) new Date(17L))).isFalse();
    }

    @Test
    public void testFluentDateEqualitySecondArgumentIsNullPassedAsObject() {
        assertThat(equalityHoldsFor((Object) new Date(18L)).and(null)).isFalse();
    }

    @Test
    public void testFluentDateEqualityFirstArgumentIsNullPassedAsObject() {
        assertThat(equalityHoldsFor(null).and((Object) new Date(18L))).isFalse();
    }


}