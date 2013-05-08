package com.elster.jupiter.util.time;

import java.text.SimpleDateFormat;
import java.util.Date;

final public class UtcInstant implements Comparable<UtcInstant> {
	
	private final long ms;
	
	public UtcInstant(long ms) {
		this.ms = ms;
	}
	
	public UtcInstant() {
		this(System.currentTimeMillis());
	}
	
	public UtcInstant(Date date) {
		this(date.getTime());
	}
	
	public long getTime() {
		return ms;
	}
	
	public Date toDate() {
		return new Date(ms);
	}
	
	@Override
	public boolean equals(Object other) {
		try {
			UtcInstant o = (UtcInstant) other;
			return ms == o.ms;
		} catch (ClassCastException ex) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return (int) ms;
	}
	
	public boolean after(UtcInstant when) {
		return ms > when.ms;
	}
	
	public boolean before(UtcInstant when) {
		return ms < when.ms;
	}
	
	public boolean after(Date when) {
		return ms > when.getTime();
	}
	
	public boolean before(Date when) {
		return ms < when.getTime();
	}
	
	public boolean afterOrEqual(UtcInstant when) {
		return ms >= when.ms;
	}
	
	public boolean beforeOrEqual(UtcInstant when) {
		return ms <= when.ms;
	}
	
	public boolean afterOrEqual(Date when) {
		return ms >= when.getTime();
	}
	
	public boolean beforeOrEqual(Date when) {
		return ms <= when.getTime();
	}
	
	@Override
	// convert to IS08601 format
	public String toString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		return format.format(this.toDate());
		
	}

	@Override
	public int compareTo(UtcInstant o) {
		return Long.signum(ms - o.ms);
	}
	
}
