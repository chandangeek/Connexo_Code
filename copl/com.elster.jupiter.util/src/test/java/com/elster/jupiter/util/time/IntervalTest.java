package com.elster.jupiter.util.time;

import org.fest.assertions.api.BooleanAssert;
import org.junit.Test;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

public class IntervalTest {
    private final Date date1 = new Date(1000);
    private final Date date2 = new Date(2000);
    private final Date date3 = new Date(3000);
    private final Date date4 = new Date(4000);
    private final Date date5 = new Date(5000);
    private final Date date6 = new Date(6000);
    private final Date date7 = new Date(7000);
    private final Date date8 = new Date(8000);

    private final Interval interval = new Interval(date3, date6);

    @Test
    public void testStartsBeforeEndsAtStart() {
        assertToNotOverlap(new Interval(date1, date3));
    }

    @Test
    public void testStartsBeforeEndsBefore() {
        assertToNotOverlap(new Interval(date1, date2));
    }

    @Test
    public void testStartsBeforeEndsIn() {
        assertToOverlap(new Interval(date1, date4));
    }

    @Test
    public void testStartsBeforeEndsAtEnd() {
        assertToOverlap(new Interval(date1, date6));
    }

    @Test
    public void testStartsBeforeEndsAfter() {
        assertToOverlap(new Interval(date1, date7));
    }

    @Test
    public void testStartsAtStartEndsAtStart() {
        assertToNotOverlap(new Interval(date3, date3));
    }

    @Test
    public void testStartsAtStartEndsIn() {
        assertToOverlap(new Interval(date3, date4));
    }

    @Test
    public void testStartsAtStartEndsAtEnd() {
        assertToOverlap(new Interval(date3, date6));
    }

    @Test
    public void testStartsAtStartEndsAfter() {
        assertToOverlap(new Interval(date3, date7));
    }

    @Test
    public void testStartsWithinEndsIn() {
        assertToOverlap(new Interval(date4, date5));
    }

    @Test
    public void testStartsWithinEndsAtEnd() {
        assertToOverlap(new Interval(date4, date6));
    }

    @Test
    public void testStartsWithinEndsAfter() {
        assertToOverlap(new Interval(date4, date7));
    }

    @Test
    public void testStartsAtEndEndsAtEnd() {
        assertToNotOverlap(new Interval(date6, date6));
    }

    @Test
    public void testStartsAtEndEndsAfter() {
        assertToNotOverlap(new Interval(date6, date7));
    }

    @Test
    public void testStartsAfterEndsAfter() {
        assertToNotOverlap(new Interval(date7, date8));
    }

    private BooleanAssert assertCommutative(Interval other) {
        return assertThat(interval.overlaps(other)).isEqualTo(other.overlaps(interval));
    }

    private void assertToNotOverlap(Interval other) {
        assertCommutative(other).isFalse();
    }

    private void assertToOverlap(Interval other) {
        assertCommutative(other).isTrue();
    }
}
