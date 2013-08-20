package com.elster.jupiter.util.time;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * UtcInstant represents a moment in time with millisecond precision.
 */
public final class UtcInstant implements Comparable<UtcInstant> {
	
	private final long ms;

    /**
     * @param ms the number of milliseconds since the Java epoch.
     */
	public UtcInstant(long ms) {
		this.ms = ms;
	}

    /**
     * Creates a new instance representing the current System's time according to the supplied Clock.
     * @param clock
     */
	public UtcInstant(Clock clock) {
		this(clock.now().getTime());
	}

    /**
     * Creates a new instance representing the same time as the supplied Date.
     * @param date
     */
	public UtcInstant(Date date) {
		this(date.getTime());
	}

    /**
     * @return the number of milliseconds since the Java epoch.
     */
	public long getTime() {
		return ms;
	}
	
	public Date toDate() {
		return new Date(ms);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UtcInstant that = (UtcInstant) o;

        return ms == that.ms;

    }

    @Override
    public int hashCode() {
        return (int) (ms ^ (ms >>> 32));
    }

    /**
     *
     * @param when
     * @return true if this instance is chronologically later than the given instance, false otherwise.
     */
	public boolean after(UtcInstant when) {
		return ms > when.ms;
	}

    /**
     *
     * @param when
     * @return true if this instance is chronologically earlier than the given instance, false otherwise.
     */
    public boolean before(UtcInstant when) {
		return ms < when.ms;
	}

    /**
     * @param when
     * @return true if this instance is chronologically later than the given Date, false otherwise.
     */
	public boolean after(Date when) {
		return ms > when.getTime();
	}

    /**
     *
     * @param when
     * @return true if this instance is chronologically earlier than the given Date, false otherwise.
     */
	public boolean before(Date when) {
		return ms < when.getTime();
	}

    /**
     *
     * @param when
     * @return true if this instance is chronologically later than or equal to the given instance, false otherwise.
     */
	public boolean afterOrEqual(UtcInstant when) {
		return ms >= when.ms;
	}

    /**
     *
     * @param when
     * @return true if this instance is chronologically earlier than or equal to the given instance, false otherwise.
     */
	public boolean beforeOrEqual(UtcInstant when) {
		return ms <= when.ms;
	}

    /**
     *
     * @param when
     * @return true if this instance is chronologically later than or at the same time as the given Date, false otherwise.
     */
	public boolean afterOrEqual(Date when) {
		return ms >= when.getTime();
	}

    /**
     *
     * @param when
     * @return  true if this instance is chronologically earlier than or at the same time as the given Date, false otherwise.
     */
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
