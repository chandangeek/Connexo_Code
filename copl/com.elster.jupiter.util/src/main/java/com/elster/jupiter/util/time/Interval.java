package com.elster.jupiter.util.time;

import java.util.Date;

public final class Interval {
	public static final long ETERNITY = 1_000_000_000_000_000_000L;
	
	private final long start;
	private final long end;
	
	public Interval(Date start, Date end) {
		this.start = getStartValue(start);
		this.end = getEndValue(end);
        if (this.start > this.end) {
            throw new IllegalArgumentException("Start cannot be later than end.");
        }
	}
		
    public static Interval startAt(Date start) {
        return new Interval(start, null);
    }
	
	public Date getEnd() {
		return end == ETERNITY ? null : new Date(end);
	}

	public Date getStart() {
		return start == -ETERNITY ? null : new Date(start);
	}
	
	public boolean isCurrent(Clock clock) {
		long now = clock.now().getTime();
		return start <= now && now < end;
	}

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
}
