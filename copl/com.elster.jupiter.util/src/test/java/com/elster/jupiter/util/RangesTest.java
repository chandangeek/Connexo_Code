/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.util.function.Function;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RangesTest {

    @Test
    public void testMapAll() {
        assertThat(Ranges.map(Range.all(), indexInAlphabet())).isEqualTo(Range.<Integer>all());
    }

    private Function<Character, Integer> indexInAlphabet() {
        return c -> c - 'A' + 1;
    }

    @Test
    public void testMapAtMost() {
        assertThat(Ranges.map(Range.atMost('T'), indexInAlphabet())).isEqualTo(Range.atMost(20));
    }

    @Test
    public void testMapLessThan() {
        assertThat(Ranges.map(Range.lessThan('T'), indexInAlphabet())).isEqualTo(Range.lessThan(20));
    }

    @Test
    public void testMapAtLeast() {
        assertThat(Ranges.map(Range.atLeast('F'), indexInAlphabet())).isEqualTo(Range.atLeast(6));
    }

    @Test
    public void testMapGreaterThan() {
        assertThat(Ranges.map(Range.greaterThan('F'), indexInAlphabet())).isEqualTo(Range.greaterThan(6));
    }

    @Test
    public void testMapClosed() {
        assertThat(Ranges.map(Range.closed('F', 'T'), indexInAlphabet())).isEqualTo(Range.closed(6, 20));
    }

    @Test
    public void testMapOpenClosed() {
        assertThat(Ranges.map(Range.openClosed('F', 'T'), indexInAlphabet())).isEqualTo(Range.openClosed(6, 20));
    }

    @Test
    public void testMapClosedOpen() {
        assertThat(Ranges.map(Range.closedOpen('F', 'T'), indexInAlphabet())).isEqualTo(Range.closedOpen(6, 20));
    }

    @Test
    public void testMapOpen() {
        assertThat(Ranges.map(Range.open('F', 'T'), indexInAlphabet())).isEqualTo(Range.open(6, 20));
    }

    @Test
    public void toOpenClosedForOpenClosedRangeWithEndPoints() {
        Range<Character> originalRange = Range.openClosed('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpenClosed();

        // Asserts
        assertThat(copied).isEqualTo(originalRange);
    }

    @Test
    public void toOpenClosedForRangeWithOnlyLowerEndPoint() {
        Range<Character> originalRange = Range.atLeast('R');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpenClosed();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.hasUpperBound()).isFalse();
    }

    @Test
    public void toOpenClosedForRangeWithoutEndPoints() {
        Range<Character> originalRange = Range.all();

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpenClosed();

        // Asserts
        assertThat(copied.hasLowerBound()).isFalse();
        assertThat(copied.hasUpperBound()).isFalse();
    }

    @Test
    public void toOpenClosedForClosedOpenRangeWithEndPoints() {
        Range<Character> originalRange = Range.closedOpen('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpenClosed();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toOpenClosedForClosedClosedRangeWithEndPoints() {
        Range<Character> originalRange = Range.closed('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpenClosed();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toOpenClosedForOpenOpenRangeWithEndPoints() {
        Range<Character> originalRange = Range.open('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpenClosed();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toClosedOpenForOpenClosedRangeWithEndPoints() {
        Range<Character> originalRange = Range.openClosed('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosedOpen();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toClosedOpenForRangeWithOnlyLowerEndPoint() {
        Range<Character> originalRange = Range.atLeast('R');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosedOpen();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.hasUpperBound()).isFalse();
    }

    @Test
    public void toClosedOpenForRangeWithoutEndPoints() {
        Range<Character> originalRange = Range.all();

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosedOpen();

        // Asserts
        assertThat(copied.hasLowerBound()).isFalse();
        assertThat(copied.hasUpperBound()).isFalse();
    }

    @Test
    public void toClosedOpenForClosedOpenRangeWithEndPoints() {
        Range<Character> originalRange = Range.closedOpen('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosedOpen();

        // Asserts
        assertThat(copied).isEqualTo(originalRange);
    }

    @Test
    public void toClosedOpenForClosedClosedRangeWithEndPoints() {
        Range<Character> originalRange = Range.closed('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosedOpen();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toClosedOpenForOpenOpenRangeWithEndPoints() {
        Range<Character> originalRange = Range.open('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosedOpen();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toOpenForOpenRangeWithEndPoints() {
        Range<Character> originalRange = Range.open('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpen();

        // Asserts
        assertThat(copied).isEqualTo(originalRange);
    }

    @Test
    public void toOpenForRangeWithoutLowerEndPoint() {
        Range<Character> originalRange = Range.atMost('V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpen();

        // Asserts
        assertThat(copied.hasLowerBound()).isFalse();
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toOpenForClosedRangeWithoutUpperEndPoint() {
        Range<Character> originalRange = Range.atLeast('V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpen();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('V');
        assertThat(copied.hasUpperBound()).isFalse();
    }

    @Test
    public void toOpenForClosedRangeWithEndPoints() {
        Range<Character> originalRange = Range.closed('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpen();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toOpenForClosedOpenRangeWithEndPoints() {
        Range<Character> originalRange = Range.closedOpen('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpen();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toOpenForOpenClosedRangeWithEndPoints() {
        Range<Character> originalRange = Range.openClosed('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asOpen();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toClosedForOpenRangeWithEndPoints() {
        Range<Character> originalRange = Range.open('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosed();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toClosedForRangeWithoutLowerEndPoint() {
        Range<Character> originalRange = Range.atMost('V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosed();

        // Asserts
        assertThat(copied.hasLowerBound()).isFalse();
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toClosedForClosedRangeWithoutUpperEndPoint() {
        Range<Character> originalRange = Range.atLeast('V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosed();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('V');
        assertThat(copied.hasUpperBound()).isFalse();
    }

    @Test
    public void toClosedForClosedRangeWithEndPoints() {
        Range<Character> originalRange = Range.closed('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosed();

        // Asserts
        assertThat(copied).isEqualTo(originalRange);
    }

    @Test
    public void toClosedForClosedOpenRangeWithEndPoints() {
        Range<Character> originalRange = Range.closedOpen('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosed();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void toClosedForOpenClosedRangeWithEndPoints() {
        Range<Character> originalRange = Range.openClosed('R', 'V');

        // Business method
        Range<Character> copied = Ranges.copy(originalRange).asClosed();

        // Asserts
        assertThat(copied.lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasLowerBound()).isTrue();
        assertThat(copied.lowerEndpoint()).isEqualTo('R');
        assertThat(copied.upperBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(copied.hasUpperBound()).isTrue();
        assertThat(copied.upperEndpoint()).isEqualTo('V');
    }

    @Test
    public void testOptionalIntersection() {
        assertThat(Ranges.intersection(Range.openClosed(1, 3), Range.atLeast(3))).contains(Range.singleton(3));
        assertThat(Ranges.intersection(Range.openClosed(1, 3), Range.greaterThan(3))).contains(Range.openClosed(3, 3));
        assertThat(Ranges.intersection(Range.openClosed(1, 3), Range.atLeast(1))).contains(Range.openClosed(1, 3));
        assertThat(Ranges.intersection(Range.closedOpen(1, 3), Range.lessThan(1))).contains(Range.closedOpen(1, 1));
        assertThat(Ranges.intersection(Range.openClosed(1, 3), Range.lessThan(1))).isEmpty();
        assertThat(Ranges.intersection(Range.open(1, 3), Range.greaterThan(3))).isEmpty();
        assertThat(Ranges.intersection(Range.lessThan(1), Range.greaterThan(1))).isEmpty();
        assertThat(Ranges.intersection(Range.closedOpen(1, 1), Range.closedOpen(1, 1))).contains(Range.closedOpen(1, 1));
        assertThat(Ranges.intersection(Range.closedOpen(1, 1), Range.openClosed(1, 1))).isEmpty();
        assertThat(Ranges.intersection(Range.greaterThan(3), Range.closed(3, 4), Range.lessThan(4))).contains(Range.open(3, 4));
        assertThat(Ranges.intersection(Range.lessThan(3), Range.lessThan(4), Range.openClosed(3, 4))).isEmpty();
        assertThat(Ranges.intersection(Range.lessThan(3), Range.lessThan(4), Range.closed(3, 4))).contains(Range.closedOpen(3, 3));
    }

    @Test
    public void testOptionalNonEmptyIntersection() {
        assertThat(Ranges.nonEmptyIntersection(Range.openClosed(1, 3), Range.atLeast(3))).contains(Range.singleton(3));
        assertThat(Ranges.nonEmptyIntersection(Range.openClosed(1, 3), Range.greaterThan(3))).isEmpty();
        assertThat(Ranges.nonEmptyIntersection(Range.openClosed(1, 3), Range.atLeast(1))).contains(Range.openClosed(1, 3));
        assertThat(Ranges.nonEmptyIntersection(Range.closedOpen(1, 3), Range.lessThan(1))).isEmpty();
        assertThat(Ranges.nonEmptyIntersection(Range.openClosed(1, 3), Range.lessThan(1))).isEmpty();
        assertThat(Ranges.nonEmptyIntersection(Range.open(1, 3), Range.greaterThan(3))).isEmpty();
        assertThat(Ranges.nonEmptyIntersection(Range.lessThan(1), Range.greaterThan(1))).isEmpty();
        assertThat(Ranges.nonEmptyIntersection(Range.closedOpen(1, 1), Range.closedOpen(1, 1))).isEmpty();
        assertThat(Ranges.nonEmptyIntersection(Range.closedOpen(1, 1), Range.openClosed(1, 1))).isEmpty();
        assertThat(Ranges.nonEmptyIntersection(Range.greaterThan(3), Range.closed(3, 4), Range.lessThan(4))).contains(Range.open(3, 4));
        assertThat(Ranges.nonEmptyIntersection(Range.lessThan(3), Range.lessThan(4), Range.openClosed(3, 4))).isEmpty();
        assertThat(Ranges.nonEmptyIntersection(Range.lessThan(3), Range.lessThan(4), Range.closed(3, 4))).isEmpty();
    }

    @Test
    public void testFlip() {
        assertThat(Ranges.flip(BoundType.CLOSED)).isSameAs(BoundType.OPEN);
        assertThat(Ranges.flip(BoundType.OPEN)).isSameAs(BoundType.CLOSED);
    }

    @Test
    public void testWithUpperBound() {
        Range<Integer> source = Range.openClosed(1, 3);
        assertThat(Ranges.copy(source).withUpperBound(5, BoundType.CLOSED)).isEqualTo(Range.openClosed(1, 5));
        assertThat(Ranges.copy(source).withUpperBound(5, BoundType.OPEN)).isEqualTo(Range.open(1, 5));

        source = Range.atLeast(1);
        assertThat(Ranges.copy(source).withUpperBound(5, BoundType.CLOSED)).isEqualTo(Range.closed(1, 5));
        assertThat(Ranges.copy(source).withUpperBound(5, BoundType.OPEN)).isEqualTo(Range.closedOpen(1, 5));

        source = Range.lessThan(1);
        assertThat(Ranges.copy(source).withUpperBound(5, BoundType.CLOSED)).isEqualTo(Range.atMost(5));
        assertThat(Ranges.copy(source).withUpperBound(5, BoundType.OPEN)).isEqualTo(Range.lessThan(5));
    }

    @Test
    public void testWithLowerBound() {
        Range<Integer> source = Range.openClosed(1, 3);
        assertThat(Ranges.copy(source).withLowerBound(-1, BoundType.CLOSED)).isEqualTo(Range.closed(-1, 3));
        assertThat(Ranges.copy(source).withLowerBound(-1, BoundType.OPEN)).isEqualTo(Range.openClosed(-1, 3));

        source = Range.atLeast(1);
        assertThat(Ranges.copy(source).withLowerBound(-1, BoundType.CLOSED)).isEqualTo(Range.atLeast(-1));
        assertThat(Ranges.copy(source).withLowerBound(-1, BoundType.OPEN)).isEqualTo(Range.greaterThan(-1));

        source = Range.lessThan(1);
        assertThat(Ranges.copy(source).withLowerBound(-1, BoundType.CLOSED)).isEqualTo(Range.closedOpen(-1, 1));
        assertThat(Ranges.copy(source).withLowerBound(-1, BoundType.OPEN)).isEqualTo(Range.open(-1, 1));
    }

    @Test
    public void testCenter() {
        assertThat(Ranges.center(Range.atLeast(1L), Function.identity(), Double::longValue)).isEmpty();
        assertThat(Ranges.center(Range.greaterThan(1L), Function.identity(), Double::longValue)).isEmpty();
        assertThat(Ranges.center(Range.atMost(1L), Function.identity(), Double::longValue)).isEmpty();
        assertThat(Ranges.center(Range.lessThan(1L), Function.identity(), Double::longValue)).isEmpty();
        assertThat(Ranges.<Long, Long>center(Range.all(), Function.identity(), Double::longValue)).isEmpty();
        assertThat(Ranges.center(Range.openClosed(1L, 3L), Function.identity(), Double::longValue)).contains(2L);
        assertThat(Ranges.center(Range.closedOpen(1L, 3L), Function.identity(), Double::longValue)).contains(2L);
        assertThat(Ranges.center(Range.closed(1L, 3L), Function.identity(), Double::longValue)).contains(2L);
        assertThat(Ranges.center(Range.open(1L, 3L), Function.identity(), Double::longValue)).contains(2L);
        assertThat(Ranges.center(Range.openClosed(1L, 1L), Function.identity(), Double::longValue)).isEmpty();
        assertThat(Ranges.center(Range.closedOpen(1L, 1L), Function.identity(), Double::longValue)).isEmpty();
        assertThat(Ranges.center(Range.singleton(1L), Function.identity(), Double::longValue)).contains(1L);
        assertThat(Ranges.center(Range.openClosed(1L, 2L), Function.identity(), Double::longValue)).contains(1L);
        assertThat(Ranges.center(Range.closedOpen(1L, 2L), Function.identity(), Double::longValue)).contains(1L);
        assertThat(Ranges.center(Range.closed(1L, 2L), Function.identity(), Double::longValue)).contains(1L);
        assertThat(Ranges.center(Range.open(1L, 2L), Function.identity(), Double::longValue)).contains(1L);
        assertThat(Ranges.center(Range.openClosed(1L, 2L), Function.identity(), Function.identity())).contains(1.5);
        assertThat(Ranges.center(Range.closedOpen(1L, 2L), Function.identity(), Function.identity())).contains(1.5);
        assertThat(Ranges.center(Range.closed(1L, 2L), Function.identity(), Function.identity())).contains(1.5);
        assertThat(Ranges.center(Range.open(1L, 2L), Function.identity(), Function.identity())).contains(1.5);
    }
}
