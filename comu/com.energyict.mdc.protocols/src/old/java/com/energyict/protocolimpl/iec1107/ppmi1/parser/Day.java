package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class is mainly meant for debugging purposes, it can display itself in
 * an pretty and structured way.
 */
public class Day {

	private static final int SECONDS_PER_DAY = 24 * 3600;

	private int readIndex = 0;
	private int day = 0;
	private int month = 0;

	private LoadProfileStatus[] status = null;
	private Interval[] reading = null;
	private String[] readingString = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM H:mm");
	private ProfileParser profileParser;

	/**
	 * @param day
	 * @param month
	 * @param profileParser
	 * @throws IOException
	 */
	public Day(int day, int month, ProfileParser profileParser) throws IOException {
		this.profileParser = profileParser;
		Calendar c = ProtocolUtils.getCalendar(getProfileParser().getPpm().getTimeZone());
		c.set(c.get(Calendar.YEAR), month - 1, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);

		/*
		 * The meter only uses the month and the day, so the year is never
		 * stored together with the profile data. During the new year period,
		 * this can cause problems with the time stamp, because the profile data
		 * from the meter is data from the previous year, but the calendar is
		 * created with the new year (the time of the commserver). This will
		 * result in profile time stamps in the future. This bug is fixed in the
		 * following code.
		 */
		int currentMonth = ProtocolUtils.getCalendar(getProfileParser().getPpm().getTimeZone()).get(Calendar.MONTH);
		if (currentMonth < c.get(Calendar.MONTH)) {
			c.add(Calendar.YEAR, -1);
		}

		int iSec = getProfileParser().getIntegrationPeriod();
		int iPerDay = SECONDS_PER_DAY / iSec + 1;

		this.day = day;
		this.month = month;

		this.reading = new Interval[iPerDay];
		this.status = new LoadProfileStatus[iPerDay];
		this.readingString = new String[iPerDay];

		for (int i = 0; i < iPerDay; i++) {
			c.add(Calendar.SECOND, iSec);
			this.reading[i] = new Interval(getProfileParser());
			this.reading[i].setDate(c.getTime());
		}
	}

	/**
	 * @return
	 */
	public ProfileParser getProfileParser() {
		return profileParser;
	}

	/**
	 * @return
	 */
	protected boolean isEmpty() {
		for (int i = 0; i < this.reading.length; i++) {
			if (!this.reading[i].isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return
	 */
	public int getReadIndex() {
		return readIndex;
	}

	public int incReadIndex() {
		return readIndex++;
	}

	public int decReadIndex() {
		return readIndex--;
	}

	public int addToReadIndex(int value) {
		readIndex += value;
		return readIndex;
	}

	public Interval[] getReading() {
		return reading;
	}

	public String[] getReadingString() {
		return readingString;
	}

	public void setReadingString(String value, int index) {
		this.readingString[index] = value;
	}

	public LoadProfileStatus[] getStatus() {
		return status;
	}

	public void setStatus(LoadProfileStatus status, int index) {
		this.status[index] = status;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Day :: day ");
		sb.append(this.day);
		sb.append(" month ");
		sb.append(this.month);
		sb.append("\n");

		for (int i = 0; i < this.status.length; i++) {
			sb.append("[");
			sb.append(this.sdf.format(this.reading[i].getDate()));
			sb.append("]#");

			for (int ii = 0; ii < this.reading[i].getValue().length; ii++) {
				sb.append(" [");
				sb.append(this.reading[i].getValue(ii));
				sb.append("] [");
				sb.append(getProfileParser().getLoadDef().toList().get(ii));
				sb.append("]");
			}

			sb.append(" - ");
			sb.append(this.status[i]);
			sb.append(" ");
			sb.append(this.readingString[i]);
			sb.append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}

}
