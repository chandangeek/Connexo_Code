package com.elster.jupiter.util.time;

import java.util.Date;

public final class IntervalBuilder {
	private Date earliest;
	private Date latest;
	
	public void add(Date when) {
		if (earliest == null || when.before(earliest)) {
			earliest = when;
		}
		if (latest == null || when.after(latest)) {
			latest = when;
		}
	}
	
	public void add(Date when, long length) {
		add(when);
		add(new Date(when.getTime() + length));
	}
	
	public boolean hasInterval() {
		return earliest != null && latest != null;
	}
	
	public Interval getInterval() {
		if (earliest == null || latest == null) 
			throw new IllegalStateException();
		return new Interval(earliest,latest);
	}
	
}
