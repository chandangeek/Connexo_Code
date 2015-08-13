package com.elster.jupiter.util.time;

import com.google.common.collect.BoundType;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * Interval represents a Date range that may be finite or infinite at either end.
 * All instance creation methods that accept a Date instance allow null as value, and will interpret this to mean the Interval is infinite at the approriate bound.
 */
public final class Interval {
    private static final long ETERNITY = 1_000_000_000_000_000_000L;
    private static final Interval SINCE_EPOCH = new Interval(0, ETERNITY);
    private static final Interval FOREVER = new Interval(-ETERNITY, ETERNITY);

    private final long start;
    private final long end;

    /**
     * @param start Date instance marking the start of the interval, or null to signify the interval is infinite at the start
     * @param end   Date instance marking the end of the interval, or null to signify the interval is infinite at the end
     */
    private Interval(Instant start, Instant end) {
        this.start = getStartValue(start);
        this.end = getEndValue(end);
        if (this.start > this.end) {
            throw new IllegalArgumentException("Start cannot be later than end.");
        }
    }

    private Interval(long start, long end) {
        this.start = start;
        this.end = end;
        if (this.start > this.end) {
            throw new IllegalArgumentException("Start cannot be later than end.");
        }
    }

    @Deprecated
    public Interval(Date start, Date end) {
        this.start = start == null ? -ETERNITY : getStartValue(start.toInstant());
        this.end = end == null ? ETERNITY : getEndValue(end.toInstant());
    }

    @Deprecated
    public static Interval of(Instant start, Instant end) {
        return new Interval(start, end);
    }

    public static Interval of(Range<Instant> range) {
        long start = range.hasLowerBound() ? range.lowerEndpoint().toEpochMilli() : -ETERNITY;
        long end = range.hasUpperBound() ? range.upperEndpoint().toEpochMilli() : ETERNITY;
        return new Interval(start, end);
    }

    public static Interval forever() {
        return FOREVER;
    }

    /**
     * Static factory method to create an infinite Interval that starts at the given Date.
     *
     * @param start
     * @return
     */
    @Deprecated
    public static Interval startAt(Instant start) {
        return new Interval(start, null);
    }

    /**
     * Static factory method to create an infinite Interval that ends at the given Date.
     *
     * @param end
     * @return
     */
    @Deprecated
    public static Interval endAt(Instant end) {
        return new Interval(null, end);
    }

    @Deprecated
    public static Interval sinceEpoch() {
        return SINCE_EPOCH;
    }

    @Deprecated
    public Instant getEnd() {
        return end == ETERNITY ? null : Instant.ofEpochMilli(end);
    }

    @Deprecated
    public Instant getStart() {
        return start == -ETERNITY ? null : Instant.ofEpochMilli(start);
    }

    /**
     * As this method is used for validity check, the CLOSED_OPEN endpoint behavior is used.
     *
     * @param clock
     * @return true if the current time according to the given Clock is contained in this Interval.
     */
    @Deprecated
    public boolean isCurrent(Clock clock) {
        long now = clock.instant().toEpochMilli();
        return contains(now, EndpointBehavior.CLOSED_OPEN);
    }

    @Deprecated
    public boolean isEffective(Interval interval) {
        return this.overlaps(interval);
    }

    public Range<Instant> toOpenClosedRange() {
        return (start == -ETERNITY) ?
                (end == ETERNITY ? Range.all() : Range.lessThan(Instant.ofEpochMilli(end))) :
                (end == ETERNITY ?
                        Range.greaterThan(Instant.ofEpochMilli(start)) :
                        Range.openClosed(Instant.ofEpochMilli(start), Instant.ofEpochMilli(this.end)));
    }

    public Range<Instant> toClosedOpenRange() {
        return (start == -ETERNITY) ?
                (end == ETERNITY ? Range.all() : Range.lessThan(Instant.ofEpochMilli(end))) :
                (end == ETERNITY ?
                        Range.atLeast(Instant.ofEpochMilli(start)) :
                        Range.closedOpen(Instant.ofEpochMilli(start), Instant.ofEpochMilli(this.end)));
    }

    public Range<Instant> toClosedRange() {
        return (start == -ETERNITY) ?
                (end == ETERNITY ? Range.all() : Range.atMost(Instant.ofEpochMilli(end))) :
                (end == ETERNITY ?
                        Range.atLeast(Instant.ofEpochMilli(start)) :
                        Range.closed(Instant.ofEpochMilli(start), Instant.ofEpochMilli(this.end)));
    }

    public Range<Instant> toOpenRange() {
        return (start == -ETERNITY) ?
                (end == ETERNITY ? Range.all() : Range.lessThan(Instant.ofEpochMilli(end))) :
                (end == ETERNITY ?
                        Range.greaterThan(Instant.ofEpochMilli(start)) :
                        Range.open(Instant.ofEpochMilli(start), Instant.ofEpochMilli(this.end)));
    }

    @Deprecated
    public Range<Instant> toRange(EndpointBehavior endpointBehavior) {
        return endpointBehavior.toRange(this);
    }

    @Deprecated
    private boolean contains(long when, EndpointBehavior behavior) {
        return behavior.contains(this, when);
    }

    /**
     * @param other
     * @return true if there is at least one Date instance different that would be contained in both this Interval, as in the given Interval, false otherwise.
     */
    @Deprecated
    public boolean overlaps(Interval other) {
        return other.end > start && end > other.start;
    }

    @SuppressWarnings("unused")
    private Interval() {
        start = 0;
        end = 0;
    }

    private long getEndValue(Instant endInstant) {
        if (endInstant == null) {
            return ETERNITY;
        } else {
            if (endInstant.toEpochMilli() >= ETERNITY) {
                throw new IllegalArgumentException("End instant too late");
            } else {
                return endInstant.toEpochMilli();
            }
        }
    }

    private long getStartValue(Instant startInstant) {
        if (startInstant == null) {
            return -ETERNITY;
        } else {
            if (startInstant.toEpochMilli() <= -ETERNITY) {
                throw new IllegalArgumentException("Start instant too early");
            } else {
                return startInstant.toEpochMilli();
            }
        }
    }

    @Override
    public String toString() {
        return "Interval{" +
                "start=" + getStart() +
                ", end=" + getEnd() +
                '}';
    }

    /**
     * @param date
     * @return true if the given instance is contained within this Date range.
     */
    @Deprecated
    public boolean contains(Instant date, EndpointBehavior behavior) {
        return contains(Objects.requireNonNull(date).toEpochMilli(), behavior);
    }

    @Deprecated
    public Interval withEnd(Instant instant) {
        return new Interval(start, getEndValue(instant));
    }

    @Deprecated
    public Interval withStart(Instant instant) {
        return new Interval(getStartValue(instant), end);
    }

    @Deprecated
    public Interval intersection(Interval interval) {
        if (!overlaps(interval)) {
            throw new IllegalArgumentException();
        }
        return new Interval(Ordering.natural().max(start, interval.start), Ordering.natural().min(end, interval.end));
    }

    @Deprecated
    public boolean includes(Interval other) {
        return !(startsAfter(other.getStart()) || endsBefore(other.getEnd()));
    }

    @Deprecated
    public boolean startsAfter(Instant testDate) {
        if (getStart() == null) {
            return false;
        }
        return testDate == null || getStart().isAfter(testDate);
    }

    @Deprecated
    public boolean endsBefore(Instant testDate) {
        if (getEnd() == null) {
            return false;
        }
        return testDate == null || getEnd().isBefore(testDate);
    }

    @Deprecated
    public boolean startsBefore(Instant testDate) {
        if (getStart() == null) {
            return testDate != null;
        }
        return testDate != null && getStart().isBefore(testDate);
    }

    @Deprecated
    public boolean endsAfter(Instant testDate) {
        if (getEnd() == null) {
            return testDate != null;
        }
        return testDate != null && getEnd().isAfter(testDate);
    }


    /**
     * Tests whether this {@link Interval} envelops the second one. This means that the first includes the second, but
     * neither from, nor to values are equal.
     *
     * @param contained
     * @return true if the first {@link Interval} envelops the second one.
     */
    @Deprecated
    public boolean envelops(Interval contained) {
        return startsBefore(contained.getStart()) && endsAfter(contained.getEnd());
    }

    /**
     * Determines whether the two given {@link Interval}s abut. Two {@link Interval}s abut if the to value of one, equals the
     * from of the other.
     *
     * @param second the second
     * @return true if both {@link Interval}s abut, false otherwise.
     */
    @Deprecated
    public boolean abuts(Interval second) {
        return fromAbuts(second) || toAbuts(second);
    }

    /**
     * @return the duration in milliseconds
     * @throws java.lang.IllegalStateException if the interval is infinite in duration.
     */
    @Deprecated
    public long durationInMillis() {
        if (isInfinite()) {
            throw new IllegalStateException();
        }
        return end - start;
    }

    @Deprecated
    public boolean isInfinite() {
        return start == -ETERNITY || end == ETERNITY;
    }

    /**
     * Determines whether the first {@link Interval} abuts the second at the first's from. (i.e. the first starts at the point
     * where the second one ends).
     *
     * @param second the second
     * @return true if the first {@link Interval} abuts the second at the first's from, false otherwise.
     */
    private boolean fromAbuts(Interval second) {
        return getStart() != null && getStart().equals(second.getEnd());
    }

    /**
     * Determines whether the first {@link Interval} abuts the second at the first's to. (i.e. the first ends at the point where
     * the second one starts).
     *
     * @param second the second
     * @return true if the first {@link Interval} abuts the second at the first's to, false otherwise.
     */
    private boolean toAbuts(Interval second) {
        return getEnd() != null && getEnd().equals(second.getStart());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Interval interval = (Interval) o;

        return end == interval.end && start == interval.start;

    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Deprecated
    public boolean isEmpty() {
        return start == end && start != -ETERNITY && end != ETERNITY;
    }

    @Deprecated
    public Interval spanToInclude(Interval other) {
        if (this.includes(other)) {
            return this;
        }
        if (other.includes(this)) {
            return other;
        }
        return new Interval(Ordering.natural().min(start, other.start), Ordering.natural().max(end, other.end));
    }

    @Deprecated
    public Interval spanToInclude(Instant date) {
        if (this.contains(date, EndpointBehavior.CLOSED_CLOSED)) {
            return this;
        }
        return new Interval(Ordering.natural().min(start, date.toEpochMilli()), Ordering.natural().max(end, date.toEpochMilli()));
    }

    @Deprecated
    public long dbStart() {
        return start;
    }

    @Deprecated
    public long dbEnd() {
        return end;
    }

    @Deprecated
    public enum EndpointBehavior {
        CLOSED_OPEN {
            boolean contains(Interval interval, long when) {
                return when >= interval.start && when < interval.end;
            }

            @Override
            public Range<Instant> toRange(Interval interval) {
                return interval.toClosedOpenRange();
            }
        },
        OPEN_CLOSED {
            boolean contains(Interval interval, long when) {
                return when > interval.start && when <= interval.end;
            }

            @Override
            public Range<Instant> toRange(Interval interval) {
                return interval.toOpenClosedRange();
            }
        },
        CLOSED_CLOSED {
            boolean contains(Interval interval, long when) {
                return when >= interval.start && when <= interval.end;
            }


            @Override
            public Range<Instant> toRange(Interval interval) {
                return interval.toClosedRange();
            }
        },
        OPEN_OPEN {
            boolean contains(Interval interval, long when) {
                return when > interval.start && when < interval.end;
            }

            @Override
            public Range<Instant> toRange(Interval interval) {
                return interval.toOpenRange();
            }
        };

        abstract boolean contains(Interval interval, long when);

        public abstract Range<Instant> toRange(Interval interval);

        public static EndpointBehavior fromRange(Range<?> range) {
            if (BoundType.CLOSED.equals(range.lowerBoundType())) {
                if (BoundType.CLOSED.equals(range.upperBoundType())) {
                    return CLOSED_CLOSED;
                } else {
                    return CLOSED_OPEN;
                }
            } else {
                if (BoundType.CLOSED.equals(range.upperBoundType())) {
                    return OPEN_CLOSED;
                } else {
                    return OPEN_OPEN;
                }
            }
        }
    }

}
