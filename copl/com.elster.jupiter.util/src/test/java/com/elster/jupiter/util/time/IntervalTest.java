package com.elster.jupiter.util.time;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;

import org.assertj.core.api.BooleanAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;

import static com.elster.jupiter.util.time.Interval.EndpointBehavior.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntervalTest extends EqualsContractTest {
    private final Instant date1 = Instant.ofEpochMilli(1000);
    private final Instant date2 = Instant.ofEpochMilli(2000);
    private final Instant date3 = Instant.ofEpochMilli(3000);
    private final Instant date4 = Instant.ofEpochMilli(4000);
    private final Instant date5 = Instant.ofEpochMilli(5000);
    private final Instant date6 = Instant.ofEpochMilli(6000);
    private final Instant date7 = Instant.ofEpochMilli(7000);
    private final Instant date8 = Instant.ofEpochMilli(8000);

    private final Interval interval = Interval.of(date3, date6);


    @Mock
    private Clock clock;



    @Test
    public void testStartsBeforeEndsAtStart() {
        assertToNotOverlap(Interval.of(date1, date3));
    }

    @Test
    public void testStartsBeforeEndsBefore() {
        assertToNotOverlap(Interval.of(date1, date2));
    }

    @Test
    public void testStartsBeforeEndsIn() {
        assertToOverlap(Interval.of(date1, date4));
    }

    @Test
    public void testStartsBeforeEndsAtEnd() {
        assertToOverlap(Interval.of(date1, date6));
    }

    @Test
    public void testStartsBeforeEndsAfter() {
        assertToOverlap(Interval.of(date1, date7));
    }

    @Test
    public void testStartsAtStartEndsAtStart() {
        assertToNotOverlap(Interval.of(date3, date3));
    }

    @Test
    public void testStartsAtStartEndsIn() {
        assertToOverlap(Interval.of(date3, date4));
    }

    @Test
    public void testStartsAtStartEndsAtEnd() {
        assertToOverlap(Interval.of(date3, date6));
    }

    @Test
    public void testStartsAtStartEndsAfter() {
        assertToOverlap(Interval.of(date3, date7));
    }

    @Test
    public void testStartsWithinEndsIn() {
        assertToOverlap(Interval.of(date4, date5));
    }

    @Test
    public void testStartsWithinEndsAtEnd() {
        assertToOverlap(Interval.of(date4, date6));
    }

    @Test
    public void testStartsWithinEndsAfter() {
        assertToOverlap(Interval.of(date4, date7));
    }

    @Test
    public void testStartsAtEndEndsAtEnd() {
        assertToNotOverlap(Interval.of(date6, date6));
    }

    @Test
    public void testStartsAtEndEndsAfter() {
        assertToNotOverlap(Interval.of(date6, date7));
    }

    @Test
    public void testStartsAfterEndsAfter() {
        assertToNotOverlap(Interval.of(date7, date8));
    }

    @Test
    public void testWithEnd() {
        Interval newInterval = Interval.of(date2, date3).withEnd(date4);
        assertThat(newInterval.getStart()).isEqualTo(date2);
        assertThat(newInterval.getEnd()).isEqualTo(date4);
    }

    @Test
    public void testWithStart() {
        Interval newInterval = Interval.of(date2, date3).withStart(date1);
        assertThat(newInterval.getStart()).isEqualTo(date1);
        assertThat(newInterval.getEnd()).isEqualTo(date3);
    }

    @Test
    public void testWithEndFromOpenEnded() {
        Interval newInterval = Interval.of(date2, null).withEnd(date4);
        assertThat(newInterval.getStart()).isEqualTo(date2);
        assertThat(newInterval.getEnd()).isEqualTo(date4);
    }

    @Test
    public void testWithStartFromOpenEnded() {
        Interval newInterval = Interval.of(null, date3).withStart(date1);
        assertThat(newInterval.getStart()).isEqualTo(date1);
        assertThat(newInterval.getEnd()).isEqualTo(date3);
    }

    @Test
    public void testWithEndToOpenEnded() {
        Interval newInterval = Interval.of(date2, date3).withEnd((Instant) null);
        assertThat(newInterval.getStart()).isEqualTo(date2);
        assertThat(newInterval.getEnd()).isNull();
    }

    @Test
    public void testWithStartToOpenEnded() {
        Interval newInterval = Interval.of(date2, date3).withStart(null);
        assertThat(newInterval.getStart()).isNull();
        assertThat(newInterval.getEnd()).isEqualTo(date3);
    }

    @Test
    public void testFiniteIntervalContainsTrueWithin() {
    	for (Interval.EndpointBehavior each : Interval.EndpointBehavior.values()) {
    		assertThat(Interval.of(date2, date4).contains(date3,each)).isTrue();
    	}
    	assertThat(Interval.of(date2,date4).toClosedOpenRange().contains(date3)).isTrue();
    	assertThat(Interval.of(date2,date4).toOpenRange().contains(date3)).isTrue();
    	assertThat(Interval.of(date2,date4).toClosedRange().contains(date3)).isTrue();
    	assertThat(Interval.of(date2,date4).toOpenClosedRange().contains(date3)).isTrue();
    }

    @Test
    public void testFiniteIntervalAtStart() {
        assertThat(Interval.of(date2, date4).contains(date2,CLOSED_OPEN)).isTrue();
        assertThat(Interval.of(date2, date4).toClosedOpenRange().contains(date2)).isTrue();
        assertThat(Interval.of(date2, date4).contains(date2,CLOSED_CLOSED)).isTrue();
        assertThat(Interval.of(date2, date4).toClosedRange().contains(date2)).isTrue();
        assertThat(Interval.of(date2, date4).contains(date2,OPEN_CLOSED)).isFalse();
        assertThat(Interval.of(date2, date4).toOpenClosedRange().contains(date2)).isFalse();
        assertThat(Interval.of(date2, date4).contains(date2,OPEN_OPEN)).isFalse();
        assertThat(Interval.of(date2, date4).toOpenClosedRange().contains(date2)).isFalse();
    }

    @Test
    public void testFiniteIntervalAtEnd() {
        assertThat(Interval.of(date2, date4).contains(date4,CLOSED_OPEN)).isFalse();
        assertThat(Interval.of(date2, date4).toClosedOpenRange().contains(date4)).isFalse();
        assertThat(Interval.of(date2, date4).contains(date4,OPEN_OPEN)).isFalse();
        assertThat(Interval.of(date2, date4).toOpenRange().contains(date4)).isFalse();
        assertThat(Interval.of(date2, date4).contains(date4,CLOSED_CLOSED)).isTrue();
        assertThat(Interval.of(date2, date4).toClosedRange().contains(date4)).isTrue();
        assertThat(Interval.of(date2, date4).contains(date4,OPEN_CLOSED)).isTrue();
        assertThat(Interval.of(date2, date4).toOpenClosedRange().contains(date4)).isTrue();
    }

    @Test
    public void testInfiniteIntervalContainsTrueWithin() {
    	for (Interval.EndpointBehavior each : Interval.EndpointBehavior.values()) {
    		assertThat(Interval.of(null, null).contains(date3,each)).isTrue();
    	}
    	assertThat(Interval.of(null,null).toClosedOpenRange().contains(date3)).isTrue();
    	assertThat(Interval.of(null,null).toOpenRange().contains(date3)).isTrue();
    	assertThat(Interval.of(null,null).toClosedRange().contains(date3)).isTrue();
    	assertThat(Interval.of(null,null).toOpenClosedRange().contains(date3)).isTrue();
    }

    @Test
    public void testEmptyInterval() {
        assertThat(Interval.of(date2, date2).contains(date2,CLOSED_OPEN)).isFalse();
        assertThat(Interval.of(date2,date2).toClosedOpenRange().contains(date2)).isFalse();
        assertThat(Interval.of(date2, date2).contains(date2,OPEN_CLOSED)).isFalse();
        assertThat(Interval.of(date2,date2).toOpenClosedRange().contains(date2)).isFalse();
        assertThat(Interval.of(date2, date2).contains(date2,OPEN_OPEN)).isFalse();
        //assertThat(Interval.of(date2,date2).open().contains(date2.toInstant())).isFalse();
        assertThat(Interval.of(date2, date2).contains(date2,CLOSED_CLOSED)).isTrue();
        assertThat(Interval.of(date2,date2).toClosedRange().contains(date2)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreventStartAfterEnd() {
        Interval.of(date4, date3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreventStartAfterEndWithEnd() {
        Interval.of(date4, null).withEnd(date3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreventStartAfterEndWithStart() {
        Interval.of(date4, date5).withStart(date6);
    }

    @Test
    public void testCreateHalfOpenWithStart() {
        Interval newInterval = Interval.startAt(date2);
        assertThat(newInterval.getStart()).isEqualTo(date2);
        assertThat(newInterval.getEnd()).isNull();
    }

    @Test
    public void testIsCurrentTrue() {
        when(clock.instant()).thenReturn(date3);

        assertThat(Interval.startAt(date2).isCurrent(clock)).isTrue();
    }

    @Test
    public void testIsCurrentFalse() {
        when(clock.instant()).thenReturn(date3);

        assertThat(Interval.startAt(date4).isCurrent(clock)).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInterSectionForNonOverlappingThrowsException() {
        Interval intersection = Interval.of(date1, date2).intersection(Interval.of(date3, date4));
        assertThat(intersection.getStart()).isEqualTo(intersection.getEnd());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInterSectionForAbuttingThrowsException() {
        Interval intersection = Interval.of(date1, date2).intersection(Interval.of(date2, date3));
        assertThat(intersection.getStart()).isEqualTo(intersection.getEnd());
    }

    @Test
    public void testInterSectionForOverlappingStart() {
        Interval intersection = Interval.of(date4, date7).intersection(Interval.of(date2, date6));
        assertThat(intersection).isEqualTo(Interval.of(date4, date6));
    }

    @Test
    public void testInterSectionForOverlappingEnd() {
        Interval intersection = Interval.of(date4, date7).intersection(Interval.of(date5, date8));
        assertThat(intersection).isEqualTo(Interval.of(date5, date7));
    }

    @Test
    public void testInterSectionForInner() {
        Interval intersection = Interval.of(date4, date7).intersection(Interval.of(date5, date6));
        assertThat(intersection).isEqualTo(Interval.of(date5, date6));
    }

    @Test
    public void testInterSectionForOuter() {
        Interval intersection = Interval.of(date5, date6).intersection(Interval.of(date4, date7));
        assertThat(intersection).isEqualTo(Interval.of(date5, date6));
    }

    @Test
    public void testSpanToIncludeDistinctIntervals() {
        assertThat(Interval.of(date1, date2).spanToInclude(Interval.of(date5, date6))).isEqualTo(Interval.of(date1, date6));
    }

    @Test
    public void testSpanToIncludeAbutting() {
        assertThat(Interval.of(date1, date5).spanToInclude(Interval.of(date5, date6))).isEqualTo(Interval.of(date1, date6));
    }

    @Test
    public void testSpanToIncludeOverlapping() {
        assertThat(Interval.of(date1, date5).spanToInclude(Interval.of(date2, date6))).isEqualTo(Interval.of(date1, date6));
    }

    @Test
    public void testSpanToIncludeAlreadyIncluded() {
        Interval interval = Interval.of(date1, date6);
        assertThat(interval.spanToInclude(Interval.of(date2, date3))).isSameAs(interval);
    }

    @Test
    public void testSpanToIncludeIncludedInArg() {
        Interval interval = Interval.of(date1, date6);
        assertThat(Interval.of(date2, date3).spanToInclude(interval)).isSameAs(interval);
    }

    @Test
    public void testSpanToIncludeTimeBefore() {
        Interval interval = Interval.of(date3, date5);
        assertThat(interval.spanToInclude(date2)).isEqualTo(Interval.of(date2, date5));
    }

    @Test
    public void testSpanToIncludeTimeAfter() {
        Interval interval = Interval.of(date3, date5);
        assertThat(interval.spanToInclude(date6)).isEqualTo(Interval.of(date3, date6));
    }

    @Test
    public void testSpanToIncludeTimeAtStart() {
        Interval interval = Interval.of(date3, date5);
        assertThat(interval.spanToInclude(date3)).isSameAs(interval);
    }

    @Test
    public void testSpanToIncludeTimeAtEnd() {
        Interval interval = Interval.of(date3, date5);
        assertThat(interval.spanToInclude(date5)).isSameAs(interval);

    }

    @Test
    public void testSpanToIncludeTimeWithin() {
        Interval interval = Interval.of(date3, date5);
        assertThat(interval.spanToInclude(date4)).isSameAs(interval);
    }

    @Test
    public void testDuration() {
        assertThat(Interval.of(date3, date5).durationInMillis()).isEqualTo(2000L);
    }

    @Test(expected = IllegalStateException.class)
    public void testDurationForInfiniteThrowsException() {
        Interval.sinceEpoch().durationInMillis();
    }

    @Test
    public void testIsInfiniteOnFinite() {
        assertThat(Interval.of(date3, date5).isInfinite()).isFalse();
    }

    @Test
    public void testIsInfiniteOnInfinite() {
        assertThat(Interval.sinceEpoch().isInfinite()).isTrue();
    }

    @Test
    public void testStartsBeforeTrue() {
        assertThat(Interval.of(date3, date5).startsBefore(date4)).isTrue();
    }

    @Test
    public void testStartsBeforeFalse() {
        assertThat(Interval.of(date3, date5).startsBefore(date2)).isFalse();
    }

    @Test
    public void testStartsBeforeComparedToStartIsFalse() {
        assertThat(Interval.of(date3, date5).startsBefore(date3)).isFalse();
    }

    @Test
    public void testStartsBeforeForMinusEternityIsTrue() {
        assertThat(Interval.endAt(date5).startsBefore(date3)).isTrue();
    }

    @Test
    public void testStartsBeforeForMinusEternityComparedToNullIsFalse() {
        assertThat(Interval.endAt(date5).startsBefore(null)).isFalse();
    }

    @Test
    public void testStartsAfterTrue() {
        assertThat(Interval.of(date3, date5).startsAfter(date2)).isTrue();
    }

    @Test
    public void testStartsAfterFalse() {
        assertThat(Interval.of(date3, date5).startsAfter(date4)).isFalse();
    }

    @Test
    public void testStartsAfterComparedToStartIsFalse() {
        assertThat(Interval.of(date3, date5).startsAfter(date3)).isFalse();
    }

    @Test
    public void testStartsAfterForMinusEternityIsFalse() {
        assertThat(Interval.endAt(date5).startsAfter(date3)).isFalse();
    }

    @Test
    public void testStartsAfterForMinusEternityComparedToNullIsFalse() {
        assertThat(Interval.endAt(date5).startsAfter(null)).isFalse();
    }

    @Test
    public void testEndsBeforeTrue() {
        assertThat(Interval.of(date3, date5).endsBefore(date6)).isTrue();
    }

    @Test
    public void testEndsBeforeFalse() {
        assertThat(Interval.of(date3, date5).endsBefore(date4)).isFalse();
    }

    @Test
    public void testEndsBeforeComparedToEndIsFalse() {
        assertThat(Interval.of(date3, date5).endsBefore(date5)).isFalse();
    }

    @Test
    public void testEndsBeforeForEternityIsFalse() {
        assertThat(Interval.startAt(date5).endsBefore(date6)).isFalse();
    }

    @Test
    public void testEndsBeforeForEternityComparedToNullIsFalse() {
        assertThat(Interval.startAt(date5).endsBefore(null)).isFalse();
    }

    @Test
    public void testEndsAfterTrue() {
        assertThat(Interval.of(date3, date5).endsAfter(date4)).isTrue();
    }

    @Test
    public void testEndsAfterFalse() {
        assertThat(Interval.of(date3, date5).endsAfter(date6)).isFalse();
    }

    @Test
    public void testEndsAfterComparedToEndIsFalse() {
        assertThat(Interval.of(date3, date5).endsAfter(date5)).isFalse();
    }

    @Test
    public void testEndsAfterForEternityIsTrue() {
        assertThat(Interval.startAt(date5).endsAfter(date6)).isTrue();
    }

    @Test
    public void testEndsAfterForEternityComparedToNullIsFalse() {
        assertThat(Interval.startAt(date5).endsAfter(null)).isFalse();
    }

    @Test
    public void testIntervalToOpenClosedRangeDoesNotIncludeStart() {
        assertThat(Interval.startAt(date1).toOpenClosedRange().lowerBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(Interval.startAt(date1).toOpenClosedRange().contains(date1)).isFalse();
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
        return Interval.of(date3, date6);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(
                Interval.of(date1, date2),
                Interval.of(date1, date3),
                Interval.of(date1, date4),
                Interval.of(date1, date6),
                Interval.of(date1, date7),
                Interval.of(date3, date4),
                Interval.of(date3, date7),
                Interval.of(date4, date6),
                Interval.of(date4, date7),
                Interval.of(date6, date7)
                );
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    private BooleanAssert assertCommutative(Interval other) {
        return (BooleanAssert) assertThat(interval.overlaps(other)).isEqualTo(other.overlaps(interval));
    }

    private void assertToNotOverlap(Interval other) {
        assertCommutative(other).isFalse();
    }

    private void assertToOverlap(Interval other) {
        assertCommutative(other).isTrue();
    }
}
