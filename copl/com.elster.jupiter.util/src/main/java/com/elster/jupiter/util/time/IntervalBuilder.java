package com.elster.jupiter.util.time;

import java.time.Instant;

public final class IntervalBuilder {
	private Instant earliest;
	private Instant latest;
	
	public void add(Instant when) {
		if (earliest == null || when.isBefore(earliest)) {
			earliest = when;
		}
		if (latest == null || when.isAfter(latest)) {
			latest = when;
		}
	}
	
	public void add(Instant when, long length) {
		add(when);
		add(when.plusMillis(length));
	}
	
	public boolean hasInterval() {
		return earliest != null && latest != null;
	}
	
	public Interval getInterval() {
		if (earliest == null || latest == null) 
			throw new IllegalStateException();
		return Interval.of(earliest,latest);
	}
	
}
