package com.elster.jupiter.util.time;

import com.google.common.collect.Ordering;

import java.util.Date;
import java.util.Objects;

/**
 * 
 * Interval represents a Date range that may be finite or infinite at either end.
 * All instance creation methods that accept a Date instance allow null as value, and will interpret this to mean the Interval is infinite at the approriate bound.
 * 
 */
public final class Interval {
	private static final long ETERNITY = 1_000_000_000_000_000_000L;

    private final long start;
	private final long end;

    /**
     * @param start Date instance marking the start of the interval, or null to signify the interval is infinite at the start
     * @param end Date instance marking the end of the interval, or null to signify the interval is infinite at the end
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
     * Static factory method to create an infinite Interval that starts at the given Date.
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
     * As this method is used for validity check, the CLOSED_OPEN endpoint behavior is used.
     * @param clock
     * @return true if the current time according to the given Clock is contained in this Interval.
     */
	public boolean isCurrent(Clock clock) {
		long now = clock.now().getTime();
		return contains(now,EndpointBehavior.CLOSED_OPEN);
	}

    private boolean contains(long when,EndpointBehavior behavior) {
    	return behavior.contains(this,when);
    }

    /**
     * @param other
     * @return true if there is at least one Date instance different that would be contained in both this Interval, as in the given Interval, false otherwise.
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
    public boolean contains(Date date , EndpointBehavior behavior) {
        return contains(Objects.requireNonNull(date).getTime() , behavior);
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
     * @throws IllegalArgumentException if the intersection is empty.
     */
    public Interval withStart(Date date) {
        return new Interval(getStartValue(date), end);
    }

    public Interval intersection(Interval interval) {
        if (!overlaps(interval)) {
            throw new IllegalArgumentException();
        }
        return new Interval(Ordering.natural().max(start, interval.start), Ordering.natural().min(end, interval.end));
    }

    public boolean includes(Interval other) {
        return !(startsAfter(other.getStart()) || endsBefore(other.getEnd()));
    }

    public boolean startsAfter(Date testDate) {
        if (getStart() == null) {
            return false;
        }
        return testDate == null || getStart().after(testDate);
    }

    public boolean endsBefore(Date testDate) {
        if (getEnd() == null) {
            return false;
        }
        return testDate == null || getEnd().before(testDate);
    }

    public boolean startsBefore(Date testDate) {
        if (getStart() == null) {
            return  testDate != null;
        }
        return testDate != null && getStart().before(testDate);
    }

    public boolean endsAfter(Date testDate) {
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

    public Interval spanToInclude(Interval other) {
        if (this.includes(other)) {
            return this;
        }
        if (other.includes(this)) {
            return other;
        }
        return new Interval(Ordering.natural().min(start, other.start), Ordering.natural().max(end, other.end));
    }

    public Interval spanToInclude(Date date) {
        if (this.contains(date, EndpointBehavior.CLOSED_CLOSED)) {
            return this;
        }
        return new Interval(Ordering.natural().min(start, date.getTime()), Ordering.natural().max(end, date.getTime()));
    }

    public enum EndpointBehavior {
    	CLOSED_OPEN {
    		boolean contains(Interval interval , long when) {
    			return when >= interval.start && when < interval.end;
    		}
    	},
    	OPEN_CLOSED {
    		boolean contains(Interval interval , long when) {
    			return when > interval.start && when <= interval.end;
    		}
		},
    	CLOSED_CLOSED {
    		boolean contains(Interval interval , long when) {
    			return when >= interval.start && when <= interval.end;
    		}
		},
    	OPEN_OPEN {
    		boolean contains(Interval interval , long when) {
    			return when > interval.start && when < interval.end;
    		}
		};
    	
    	abstract boolean contains(Interval interval , long when);
    }
 
 }
