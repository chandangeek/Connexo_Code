package com.elster.jupiter.util.time;

import com.google.common.collect.Ordering;

import java.util.Date;
import java.util.Objects;

/**
 * Interval represents a Date range that may be close, open or half open.
 * All methods that accept a Date instance allow null as value, and will interpret this to mean the Interval is open at the appropriate bound.
 */
public final class Interval {
	public static final long ETERNITY = 1_000_000_000_000_000_000L;
    private static final int BITS_PER_INT = 32;
    private static final int PRIME = 31;

    private final long start;
	private final long end;

    /**
     * @param start Date instance marking the start of the interval, or null to signify the interval is open at the start
     * @param end Date instance marking the end of the interval, or null to signify the interval is open at the end
     */
	public Interval(Date start, Date end) {
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

    /**
     * Static factory method to create a half open Interval that starts at the given Date.
     * @param start
     * @return
     */
    public static Interval startAt(Date start) {
        return new Interval(start, null);
    }
	
	public Date getEnd() {
		return end == ETERNITY ? null : new Date(end);
	}

	public Date getStart() {
		return start == -ETERNITY ? null : new Date(start);
	}

    /**
     * @param clock
     * @return true if the current time according to the given Clock is contained in this Interval.
     */
	public boolean isCurrent(Clock clock) {
		long now = clock.now().getTime();
		return contains(now);
	}

    private boolean contains(long now) {
        return start <= now && now < end;
    }

    /**
     * @param other
     * @return true if there is at least one Date instance that would be contained in both this Interval, as in the given Interval, false otherwise.
     */
    public boolean overlaps(Interval other) {
        return other.end > start && end > other.start;
    }
	
	@SuppressWarnings("unused")
	private Interval() {
		start = 0;
		end = 0;
	}
	
	private long getEndValue(Date endDate) {
		if (endDate == null) {
			return ETERNITY;
		} else {
			if (endDate.getTime() >= ETERNITY) {
				throw new IllegalArgumentException("End date too late");
			} else {
				return endDate.getTime();
			}
		}
	}

	private long getStartValue(Date startDate) {
		if (startDate == null) {
			return -ETERNITY;
		} else {
			if (startDate.getTime() <= -ETERNITY) {
				throw new IllegalArgumentException("Start date too early");
			} else {
				return startDate.getTime();
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
    public boolean contains(Date date) {
        return contains(date.getTime());
    }

    /**
     * @param date
     * @return a new Interval with the same start specification as this one, yet with the given end Date.
     */
    public Interval withEnd(Date date) {
        return new Interval(start, getEndValue(date));
    }

    /**
     * @param date
     * @return a new Interval with the same end specification as this one, yet with the given start Date.
     */
    public Interval withStart(Date date) {
        return new Interval(getStartValue(date), end);
    }

    public Interval intersection(Interval interval) {
        if (!overlaps(interval)) {
            return new Interval(start, start);
        }
        return new Interval(Ordering.natural().max(start, interval.start), Ordering.natural().min(end, interval.end));
    }

    public boolean includes(Interval other) {
        return !(startsAfter(other.getStart()) || endsBefore(other.getEnd()));
    }

    private boolean startsAfter(Date date) {
        if (getStart() == null) {
            return false;
        }
        return date == null || getStart().after(date);
    }

    private boolean endsBefore(Date testDate) {
        if (getEnd() == null) {
            return false;
        }
        return testDate == null || getEnd().before(testDate);
    }

    private boolean startsBefore(Date testDate) {
        if (getStart() == null) {
            return  testDate != null;
        }
        return testDate != null && getStart().before(testDate);
    }

    private boolean endsAfter(Date testDate) {
        if (getEnd() == null) {
            return testDate != null;
        }
        return testDate != null && getEnd().after(testDate);
    }



    /**
     * Tests whether this {@link Interval} envelops the second one. This means that the first includes the second, but
     * neither from, nor to values are equal.
     *
     * @param contained
     * @return true if the first {@link Interval} envelops the second one.
     */
    public boolean envelops(Interval contained) {
        return startsBefore(contained.getStart()) && endsAfter(contained.getEnd());
    }

    /**
     * Determines whether the two given {@link Interval}s abut. Two {@link Interval}s abut if the to value of one, equals the
     * from of the other.
     *
     * @param second
     *            the second
     * @return true if both {@link Interval}s abut, false otherwise.
     */
    public boolean abuts(Interval second) {
        return fromAbuts(second) || toAbuts(second);
    }

    /**
     * Determines whether the first {@link Interval} abuts the second at the first's from. (i.e. the first starts at the point
     * where the second one ends).
     *
     * @param second
     *            the second
     * @return true if the first {@link Interval} abuts the second at the first's from, false otherwise.
     */
    private boolean fromAbuts(Interval second) {
        return getStart() != null && getStart().equals(second.getEnd());
    }

    /**
     * Determines whether the first {@link Interval} abuts the second at the first's to. (i.e. the first ends at the point where
     * the second one starts).
     *
     * @param second
     *            the second
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

    public boolean isEmpty() {
        return start == end && start != -ETERNITY && end != ETERNITY;
    }
}
