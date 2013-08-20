package com.elster.jupiter.util.time;

import java.util.Date;

/**
 * Interval represents a Date range that may be close, open or half open.
 * All methods that accept a Date instance allow null as value, and will interpret this to mean the Interval is open at the appropriate bound.
 */
public final class Interval {
	public static final long ETERNITY = 1_000_000_000_000_000_000L;
	
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
}
