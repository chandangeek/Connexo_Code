package com.elster.jupiter.util.time;

import com.elster.jupiter.tasks.impl.test.util.EqualsContractTest;
import org.fest.assertions.api.BooleanAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntervalTest extends EqualsContractTest {
    private final Date date1 = new Date(1000);
    private final Date date2 = new Date(2000);
    private final Date date3 = new Date(3000);
    private final Date date4 = new Date(4000);
    private final Date date5 = new Date(5000);
    private final Date date6 = new Date(6000);
    private final Date date7 = new Date(7000);
    private final Date date8 = new Date(8000);

    private final Interval interval = new Interval(date3, date6);

    @Mock
    private Clock clock;



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

    @Test
    public void testWithEnd() {
        Interval newInterval = new Interval(date2, date3).withEnd(date4);
        assertThat(newInterval.getStart()).isEqualTo(date2);
        assertThat(newInterval.getEnd()).isEqualTo(date4);
    }

    @Test
    public void testWithStart() {
        Interval newInterval = new Interval(date2, date3).withStart(date1);
        assertThat(newInterval.getStart()).isEqualTo(date1);
        assertThat(newInterval.getEnd()).isEqualTo(date3);
    }

    @Test
    public void testWithEndFromOpenEnded() {
        Interval newInterval = new Interval(date2, null).withEnd(date4);
        assertThat(newInterval.getStart()).isEqualTo(date2);
        assertThat(newInterval.getEnd()).isEqualTo(date4);
    }

    @Test
    public void testWithStartFromOpenEnded() {
        Interval newInterval = new Interval(null, date3).withStart(date1);
        assertThat(newInterval.getStart()).isEqualTo(date1);
        assertThat(newInterval.getEnd()).isEqualTo(date3);
    }

    @Test
    public void testWithEndToOpenEnded() {
        Interval newInterval = new Interval(date2, date3).withEnd(null);
        assertThat(newInterval.getStart()).isEqualTo(date2);
        assertThat(newInterval.getEnd()).isNull();
    }

    @Test
    public void testWithStartToOpenEnded() {
        Interval newInterval = new Interval(date2, date3).withStart(null);
        assertThat(newInterval.getStart()).isNull();
        assertThat(newInterval.getEnd()).isEqualTo(date3);
    }

    @Test
    public void testClosedIntervalContainsTrueWithin() {
        assertThat(new Interval(date2, date4).contains(date3)).isTrue();
    }

    @Test
    public void testClosedIntervalContainsTrueAtStart() {
        assertThat(new Interval(date2, date4).contains(date2)).isTrue();
    }

    @Test
    public void testClosedIntervalContainsFalseAtEnd() {
        assertThat(new Interval(date2, date4).contains(date4)).isFalse();
    }

    @Test
    public void testOpenIntervalContainsTrueWithin() {
        assertThat(new Interval(null, null).contains(date3)).isTrue();
    }

    @Test
    public void testEmptyIntervalContainsFalse() {
        assertThat(new Interval(date2, date2).contains(date2)).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreventStartAfterEnd() {
        new Interval(date4, date3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreventStartAfterEndWithEnd() {
        new Interval(date4, null).withEnd(date3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreventStartAfterEndWithStart() {
        new Interval(date4, date5).withStart(date6);
    }

    @Test
    public void testCreateHalfOpenWithStart() {
        Interval newInterval = Interval.startAt(date2);
        assertThat(newInterval.getStart()).isEqualTo(date2);
        assertThat(newInterval.getEnd()).isNull();
    }

    @Test
    public void testIsCurrentTrue() {
        when(clock.now()).thenReturn(date3);

        assertThat(Interval.startAt(date2).isCurrent(clock)).isTrue();
    }

    @Test
    public void testIsCurrentFalse() {
        when(clock.now()).thenReturn(date3);

        assertThat(Interval.startAt(date4).isCurrent(clock)).isFalse();
    }

    @Test
    public void testInterSectionForNonOverlappingIsEmpty() {
        Interval intersection = new Interval(date1, date2).intersection(new Interval(date3, date4));
        assertThat(intersection.getStart()).isEqualTo(intersection.getEnd());
    }

    @Test
    public void testInterSectionForAbuttingIsEmpty() {
        Interval intersection = new Interval(date1, date2).intersection(new Interval(date2, date3));
        assertThat(intersection.getStart()).isEqualTo(intersection.getEnd());
    }

    @Test
    public void testInterSectionForOverlappingStart() {
        Interval intersection = new Interval(date4, date7).intersection(new Interval(date2, date6));
        assertThat(intersection).isEqualTo(new Interval(date4, date6));
    }

    @Test
    public void testInterSectionForOverlappingEnd() {
        Interval intersection = new Interval(date4, date7).intersection(new Interval(date5, date8));
        assertThat(intersection).isEqualTo(new Interval(date5, date7));
    }

    @Test
    public void testInterSectionForInner() {
        Interval intersection = new Interval(date4, date7).intersection(new Interval(date5, date6));
        assertThat(intersection).isEqualTo(new Interval(date5, date6));
    }

    @Test
    public void testInterSectionForOuter() {
        Interval intersection = new Interval(date5, date6).intersection(new Interval(date4, date7));
        assertThat(intersection).isEqualTo(new Interval(date5, date6));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceA() {
        return interval;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new Interval(date3, date6);
    }

    @Override
    protected Object getInstanceNotEqualToA() {
        return new Interval(date2, date5);
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
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
