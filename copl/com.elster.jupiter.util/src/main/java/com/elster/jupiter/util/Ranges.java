package com.elster.jupiter.util;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Ranges {

    public static final class QueryDecorator<C extends Comparable<? super C>> {
        private final Range<C> decorated;

        public QueryDecorator(Range<C> decorated) {
            this.decorated = decorated;
        }

        /**
         * @param other
         * @return true if there is at least one Date instance different that would be contained in both this Range, as in the given Range, false otherwise.
         */
        public boolean overlap(Range<C> other) {
            return decorated.isConnected(other) && !decorated.intersection(other).isEmpty();
        }

        public boolean include(Range<C> other) {
            return decorated.encloses(other);
        }

        public boolean startAfter(C c) {
            return decorated.hasLowerBound() && (c == null || decorated.lowerEndpoint().compareTo(c) > 0);
        }

        public boolean endBefore(C c) {
            return decorated.hasUpperBound() && (c == null || decorated.upperEndpoint().compareTo(c) < 0);
        }

        public boolean startBefore(C c) {
            if (!decorated.hasLowerBound()) {
                return  c != null;
            }
            return c != null && decorated.lowerEndpoint().compareTo(c) < 0;
        }

        public boolean endAfter(C c) {
            if (!decorated.hasUpperBound()) {
                return c != null;
            }
            return c != null && decorated.upperEndpoint().compareTo(c) > 0;
        }

        /**
         * Tests whether this {@link Range} envelops the second one. This means that the first includes the second, but
         * neither from, nor to values are equal.
         *
         * @param contained
         * @return true if the first {@link Range} envelops the second one.
         */
        public boolean envelop(Range<C> contained) {
            return contained.hasLowerBound() && contained.hasUpperBound() && startBefore(contained.lowerEndpoint()) && endAfter(contained.upperEndpoint());
        }

        /**
         * Determines whether the two given {@link Range}s abut. Two {@link Range}s abut if the to value of one, equals the
         * from of the other.
         *
         * @param second
         *            the second
         * @return true if both {@link Range}s abut, false otherwise.
         */
        public boolean abut(Range<C> second) {
            return decorated.isConnected(second) && decorated.intersection(second).isEmpty();
        }

        public boolean equal(Range<C> range) {
            return decorated.equals(range);

        }

    }

    public static final class CreationDecorator<C extends Comparable<? super C>> {
        private final Range<C> decorated;

        public CreationDecorator(Range<C> decorated) {
            this.decorated = decorated;
        }

        public Range<C> withOpenLowerBound(C c) {
            if (c == null) {
                return decorated.hasUpperBound() ? Range.upTo(decorated.upperEndpoint(), decorated.upperBoundType()) : Range.<C>all();
            }
            return decorated.hasUpperBound() ? Range.range(c, BoundType.OPEN, decorated.upperEndpoint(), decorated.upperBoundType()) : Range.greaterThan(c);
        }

        public Range<C> withClosedLowerBound(C c) {
            if (c == null) {
                return decorated.hasUpperBound() ? Range.upTo(decorated.upperEndpoint(), decorated.upperBoundType()) : Range.<C>all();
            }
            return decorated.hasUpperBound() ? Range.range(c, BoundType.CLOSED, decorated.upperEndpoint(), decorated.upperBoundType()) : Range.atLeast(c);
        }

        public Range<C> withoutLowerBound() {
            return decorated.hasUpperBound() ? Range.upTo(decorated.upperEndpoint(), decorated.upperBoundType()) : Range.all();
        }

        public Range<C> withOpenUpperBound(C c) {
            if (c == null) {
                return decorated.hasLowerBound() ? Range.downTo(decorated.lowerEndpoint(), decorated.lowerBoundType()) : Range.<C>all();
            }
            return decorated.hasLowerBound() ? Range.range(decorated.lowerEndpoint(), decorated.lowerBoundType(), c, BoundType.OPEN) : Range.lessThan(c);
        }

        public Range<C> withClosedUpperBound(C c) {
            if (c == null) {
                return decorated.hasLowerBound() ? Range.downTo(decorated.lowerEndpoint(), decorated.lowerBoundType()) : Range.<C>all();
            }
            return decorated.hasLowerBound() ? Range.range(decorated.lowerEndpoint(), decorated.lowerBoundType(), c, BoundType.CLOSED) : Range.atMost(c);
        }

        public Range<C> withoutUpperBound() {
            return decorated.hasLowerBound() ? Range.downTo(decorated.lowerEndpoint(), decorated.lowerBoundType()) : Range.all();
        }

    }

    public static <C extends Comparable<? super C>> QueryDecorator<C> does(Range<C> range) {
        return new QueryDecorator<>(range);
    }

    public static <C extends Comparable<? super C>> CreationDecorator<C> copy(Range<C> range) {
        return new CreationDecorator<>(range);
    }

    public static <C extends Comparable<? super C>> Range<C> open(C start, C end) {
        return createRange(start, end, Range::greaterThan, Range::lessThan, Range::open);
    }

    public static <C extends Comparable<? super C>> Range<C> openClosed(C start, C end) {
        return createRange(start, end, Range::greaterThan, Range::atMost, Range::openClosed);
    }

    public static <C extends Comparable<? super C>> Range<C> closedOpen(C start, C end) {
        return createRange(start, end, Range::atLeast, Range::lessThan, Range::closedOpen);
    }

    public static <C extends Comparable<? super C>> Range<C> closed(C start, C end) {
        return createRange(start, end, Range::atLeast, Range::atMost, Range::closed);
    }

    public static <C extends Comparable<? super C>, T extends Comparable<? super T>> Range<T> map(Range<C> range, Function<? super C, ? extends T> function) {
        if (range.hasLowerBound()) {
            if (range.hasUpperBound()) {
                return Range.range(function.apply(range.lowerEndpoint()), range.lowerBoundType(), function.apply(range.upperEndpoint()), range.upperBoundType());
            }
            return Range.downTo(function.apply(range.lowerEndpoint()), range.lowerBoundType());
        }
        if (range.hasUpperBound()) {
            return Range.upTo(function.apply(range.upperEndpoint()), range.upperBoundType());
        }
        return Range.all();

    }

    private static <C extends Comparable<? super C>> Range<C> createRange(C start, C end, Function<C, Range<C>> ifOnlyStart, Function<C, Range<C>> ifOnlyEnd, BiFunction<C, C, Range<C>> ifBoth) {
        if (start == null) {
            if (end == null) {
                return Range.all();
            }
            return ifOnlyEnd.apply(end);
        }
        if (end == null) {
            return ifOnlyStart.apply(start);
        }
        return ifBoth.apply(start, end);
    }
}
