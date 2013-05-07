package com.elster.jupiter.time;

import java.util.Date;

public final class Interval {
	
	public static final long ETERNITY = 1_000_000_000_000_000_000L;
	
	private final long start;
	private final long end;
	
	@SuppressWarnings("unused")
	private Interval() {
		start = 0;
		end = 0;
	}
	
	public Interval(Date start, Date end) {
		this.start = getStartValue(start);
		this.end = getEndValue(end);
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
		
	public Interval(Date start) {
		this(start,null);
	}

	public Date getStart() {
		return start == -ETERNITY ? null : new Date(start);
	}
	
	public Date getEnd() {
		return end == ETERNITY ? null : new Date(end);
	}
	
	public boolean isCurrent() {
		long now = System.currentTimeMillis();
		return start <= now && now < end;
	}
}
