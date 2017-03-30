/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.emeter;

public class LoadProfileCompactArrayEntry {

	private int					value;
	private int					intervalInSeconds;
	private boolean				dst;
	private boolean				badTime;

	private int					markerType;
	private int					bit3130;

	private int					seconds;
	private int					minutes;
	private int					hours;
	private int					day;
	private int					month;
	private int					year;

	private static final int	VALUE			= 0;
	private static final int	VALUE_PARTIAL	= 1;
	private static final int	VALUE_DATE		= 2;
	private static final int	VALUE_TIME		= 3;
	private static final int[]	INTERVALS		= new int[] { 600, 900, 1800, 3600 };	// in seconds

	public LoadProfileCompactArrayEntry(long rawValue) {

		bit3130 = (int) ((rawValue >> 30) & 0x03);
		dst = (int) ((rawValue >> 21) & 1) == 1;
		badTime = (int) ((rawValue >> 29) & 1) == 1;

		switch (bit3130) {

			case VALUE_PARTIAL:
			case VALUE: {
				value = (int) (rawValue & 0x1FFFFF);
				intervalInSeconds = INTERVALS[(int) ((rawValue >> 22) & 0x3)];
			}
				break;

			case VALUE_DATE: {
				markerType = (int) (rawValue >> 24) & 0x7;
				day = (int) (rawValue) & 0x1F;
				month = ((int) (rawValue >> 5) & 0xF) - 1;
				year = (int) ((rawValue >> 9) & 0x7F) + 2000;
			}
				break;

			case VALUE_TIME: {
				markerType = (int) (rawValue >> 24) & 0x7;
				seconds = (int) (rawValue) & 0x3F;
				minutes = (int) (rawValue >> 6) & 0x3F;
				hours = (int) (rawValue >> 12) & 0x1F;
			}
				break;
		}
	}

	public boolean isValue() {
		return bit3130 == 0;
	}

	public boolean isPartialValue() {
		return bit3130 == 1;
	}

	public boolean isDate() {
		return bit3130 == 2;
	}

	public boolean isTime() {
		return bit3130 == 3;
	}

	public boolean isStartOfLoadProfile() {
		return markerType == 0;
	}

	public boolean isChangeOfIntegrationtime() {
		return markerType == 1;
	}

	public boolean isPowerOff() {
		return markerType == 2;
	}

	public boolean isPowerOn() {
		return markerType == 3;
	}

	public boolean isChangeclockOldTime() {
		return markerType == 4;
	}

	public boolean isChangeclockNewTime() {
		return markerType == 5;
	}

	@Override
	public String toString() {
		// Generated code by ToStringBuilder
		StringBuffer strBuff = new StringBuffer();
		if ((bit3130 == VALUE) || (bit3130 == VALUE_PARTIAL)) {
			strBuff.append("partialValue=" + isPartialValue() + ", ");
			strBuff.append("badTime=" + isBadTime() + ", ");
			strBuff.append("dst=" + isDst() + ", ");
			strBuff.append("intervalInSeconds=" + getIntervalInSeconds() + ", ");
			strBuff.append("value=" + getValue());
		} else if (bit3130 == VALUE_DATE) {
			strBuff.append("date=" + day + "/" + month + "/" + year + ", ");
			strBuff.append("markerType=" + getMarkerType());
		} else if (bit3130 == VALUE_TIME) {
			strBuff.append("time=" + hours + ":" + minutes + ":" + seconds + ", ");
			strBuff.append("markerType=" + getMarkerType());
		}
		return strBuff.toString();
	}

	public int getValue() {
		return value;
	}

	public int getIntervalInSeconds() {
		return intervalInSeconds;
	}

	public boolean isDst() {
		return dst;
	}

	public boolean isBadTime() {
		return badTime;
	}

	public int getMarkerType() {
		return markerType;
	}

	public int getSeconds() {
		return seconds;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getHours() {
		return hours;
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

}
