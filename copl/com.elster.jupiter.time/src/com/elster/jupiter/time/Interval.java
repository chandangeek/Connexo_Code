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
		this.start = (start == null) ? Long.MIN_VALUE : start.getTime();
		if (end == null) {
			this.end = ETERNITY;
		} else {
			if (end.getTime() > ETERNITY) {
				throw new IllegalArgumentException("End date too big " + end);
			} else {
				this.end  = end == null ? ETERNITY : end.getTime();
			}
		}
	}

	public Interval(Date start) {
		this(start,null);
	}

	public Date getStart() {
		return start == Long.MIN_VALUE ? null : new Date(start);
	}
	
	public Date getEnd() {
		return end == ETERNITY ? null : new Date(end);
	}
	
	public boolean isCurrent() {
		long now = System.currentTimeMillis();
		return start <= now && now < end;
	}
}
