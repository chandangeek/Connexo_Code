package com.energyict.protocolimpl.cm10;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class PowerFailEntry implements Comparable {

	public int compareTo(Object arg0) {
		return startTime.compareTo(((PowerFailEntry) arg0).getStartTime());
	}

	private CM10 cm10Protocol;
	private Date startTime;
	private Date endTime;
	private int duration; // in seconds;
	private boolean valid = false;

	public PowerFailEntry(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}

	public void parse(byte[] data, Calendar now) throws IOException {
		Calendar c = ProtocolUtils.getCalendar(cm10Protocol.getTimeZone());
		int currentDay = c.get(Calendar.DATE);

		int day = data[3];

		if (day == 0) {
			valid = false;
			return;
		}

		int sec = data[0];
		int min = data[1];
		int hour = data[2];

		duration = ProtocolUtils.getIntLE(data, 4, 2);

        c.set( Calendar.DAY_OF_MONTH, day );
        c.set( Calendar.HOUR_OF_DAY, hour);
        c.set( Calendar.MINUTE, min);
        c.set( Calendar.SECOND, sec);
        c.set(Calendar.MILLISECOND, 0);

        if (c.after(now))
        	c.add(Calendar.MONTH, -1);

        this.startTime = c.getTime();

        c.add(Calendar.SECOND, duration);

        this.endTime = c.getTime();

        valid = true;
	}

	public boolean isValid() {
		return valid;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	public boolean isPowerDownInterval(Date startOfInterval, Date endOfInterval) {
		return ((startTime.after(startOfInterval)) && (!startTime.after(endOfInterval)));
	}

	public boolean isPowerUpInterval(Date startOfInterval, Date endOfInterval) {
		return ((endTime.after(startOfInterval)) && (!endTime.after(endOfInterval)));
	}

	public String toString() {
		return "startTime = " + startTime + ", endTime = "  + endTime + ", duration = " + duration + " seconds";
	}

	public boolean equals(Object o) {
		if (o instanceof PowerFailEntry) {
			PowerFailEntry entry = (PowerFailEntry) o;
			return ((entry.getStartTime().equals(this.getStartTime())) &&
			        (entry.getDuration() == this.getDuration()));
		}
		return false;
	}

	public int getDuration() {
		return this.duration;
	}




}
