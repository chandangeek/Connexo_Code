package com.elster.jupiter.util.time;

import com.elster.jupiter.devtools.tests.EqualsContractTest;

import com.google.common.collect.ImmutableList;
import org.fest.assertions.api.BooleanAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static com.elster.jupiter.util.time.Interval.EndpointBehavior.*;

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
    public void testFiniteIntervalContainsTrueWithin() {
    	for (Interval.EndpointBehavior each : Interval.EndpointBehavior.values()) {
    		assertThat(new Interval(date2, date4).contains(date3,each)).isTrue();
    	}
    }

    @Test
    public void testFiniteIntervalAtStart() {
        assertThat(new Interval(date2, date4).contains(date2,CLOSED_OPEN)).isTrue();
        assertThat(new Interval(date2, date4).contains(date2,CLOSED_CLOSED)).isTrue();
        assertThat(new Interval(date2, date4).contains(date2,OPEN_CLOSED)).isFalse();
        assertThat(new Interval(date2, date4).contains(date2,OPEN_OPEN)).isFalse();
    }

    @Test
    public void testFiniteIntervalAtEnd() {
        assertThat(new Interval(date2, date4).contains(date4,CLOSED_OPEN)).isFalse();
        assertThat(new Interval(date2, date4).contains(date4,OPEN_OPEN)).isFalse();
        assertThat(new Interval(date2, date4).contains(date4,CLOSED_CLOSED)).isTrue();
        assertThat(new Interval(date2, date4).contains(date4,OPEN_CLOSED)).isTrue();
    }

    @Test
    public void testInfiniteIntervalContainsTrueWithin() {
    	for (Interval.EndpointBehavior each : Interval.EndpointBehavior.values()) {
    		assertThat(new Interval(null, null).contains(date3,each)).isTrue();
    	}
    }

    @Test
    public void testEmptyInterval() {
        assertThat(new Interval(date2, date2).contains(date2,CLOSED_OPEN)).isFalse();
        assertThat(new Interval(date2, date2).contains(date2,OPEN_CLOSED)).isFalse();
        assertThat(new Interval(date2, date2).contains(date2,OPEN_OPEN)).isFalse();
        assertThat(new Interval(date2, date2).contains(date2,CLOSED_CLOSED)).isTrue();
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

    @Test(expected = IllegalArgumentException.class)
    public void testInterSectionForNonOverlappingThrowsException() {
        Interval intersection = new Interval(date1, date2).intersection(new Interval(date3, date4));
        assertThat(intersection.getStart()).isEqualTo(intersection.getEnd());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInterSectionForAbuttingThrowsException() {
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

    @Test
    public void testSpanToIncludeDistinctIntervals() {
        assertThat(new Interval(date1, date2).spanToInclude(new Interval(date5, date6))).isEqualTo(new Interval(date1, date6));
    }

    @Test
    public void testSpanToIncludeAbutting() {
        assertThat(new Interval(date1, date5).spanToInclude(new Interval(date5, date6))).isEqualTo(new Interval(date1, date6));
    }

    @Test
    public void testSpanToIncludeOverlapping() {
        assertThat(new Interval(date1, date5).spanToInclude(new Interval(date2, date6))).isEqualTo(new Interval(date1, date6));
    }

    @Test
    public void testSpanToIncludeAlreadyIncluded() {
        Interval interval = new Interval(date1, date6);
        assertThat(interval.spanToInclude(new Interval(date2, date3))).isSameAs(interval);
    }

    @Test
    public void testSpanToIncludeIncludedInArg() {
        Interval interval = new Interval(date1, date6);
        assertThat(new Interval(date2, date3).spanToInclude(interval)).isSameAs(interval);
    }

    @Test
    public void testSpanToIncludeTimeBefore() {
        Interval interval = new Interval(date3, date5);
        assertThat(interval.spanToInclude(date2)).isEqualTo(new Interval(date2, date5));
    }

    @Test
    public void testSpanToIncludeTimeAfter() {
        Interval interval = new Interval(date3, date5);
        assertThat(interval.spanToInclude(date6)).isEqualTo(new Interval(date3, date6));
    }

    @Test
    public void testSpanToIncludeTimeAtStart() {
        Interval interval = new Interval(date3, date5);
        assertThat(interval.spanToInclude(date3)).isSameAs(interval);
    }

    @Test
    public void testSpanToIncludeTimeAtEnd() {
        Interval interval = new Interval(date3, date5);
        assertThat(interval.spanToInclude(date5)).isSameAs(interval);

    }

    @Test
    public void testSpanToIncludeTimeWithin() {
        Interval interval = new Interval(date3, date5);
        assertThat(interval.spanToInclude(date4)).isSameAs(interval);
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
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(
                new Interval(date1, date2),
                new Interval(date1, date3),
                new Interval(date1, date4),
                new Interval(date1, date6),
                new Interval(date1, date7),
                new Interval(date3, date4),
                new Interval(date3, date7),
                new Interval(date4, date6),
                new Interval(date4, date7),
                new Interval(date6, date7)
                );
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
