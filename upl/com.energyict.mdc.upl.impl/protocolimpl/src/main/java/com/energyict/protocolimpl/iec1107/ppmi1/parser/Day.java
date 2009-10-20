package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus;

/** This class is mainly meant for debugging purposes, it can display itself
 * in an pretty and structured way. */
public class Day {

	private ProfileParser	profileParser;
	private int readIndex = 0;

	private int day = 0;
	private int month = 0;

	private LoadProfileStatus[] status = null;
	private Interval[] reading = null;
	private String[] readingString = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM H:mm");

	public Day(int day, int month, ProfileParser profileParser) throws IOException {
		this.profileParser = profileParser;
		Calendar c = ProtocolUtils.getCalendar(getProfileParser().getPpm().getTimeZone() );
		c.set(c.get(Calendar.YEAR), month - 1, day, 0, 0, 0);

		int iSec = getProfileParser().getIntegrationPeriod();
		int iPerDay = 86400 /* =secs/day */ / iSec + 1;

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

	public ProfileParser getProfileParser() {
		return profileParser;
	}

	boolean isEmpty( ){
		for( int i = 0; i < this.reading.length; i ++ ) {
			if( ! this.reading[i].isEmpty() ) {
				return false;
			}
		}
		return true;
	}

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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Day :: day " + this.day + " month " + this.month + "\n");

		for (int i = 0; i < this.status.length; i++) {
			sb.append("[" + this.sdf.format(this.reading[i].getDate()) + "]#");

			for (int ii = 0; ii < this.reading[i].getValue().length; ii++) {
				sb.append(" [" + this.reading[i].getValue(ii) + "]");
				sb.append(" [" + getProfileParser().getLoadDef().toList().get(ii) + "]");
			}

			sb.append(" - " + this.status[i]);
			sb.append(" " + this.readingString[i] + "\n");
		}
		sb.append("\n");
		return sb.toString();
	}

}
