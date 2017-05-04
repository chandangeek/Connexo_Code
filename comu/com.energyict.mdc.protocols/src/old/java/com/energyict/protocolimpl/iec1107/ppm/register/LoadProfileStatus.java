/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppm.register;

import com.energyict.mdc.common.interval.IntervalStateBits;

/**
 * can parse a status from a byte, and display itself.
 * @author fbo
 */

public class LoadProfileStatus {

	/* LoadProfileStatus Type */
	private static final byte SS_METER_RESET = 0x01;
	private static final byte SS_TIME_CHANGE = 0x02;
	private static final byte SS_OTHER_CHANGE = 0x04;
	private static final byte SS_BATTERY_MONITOR = 0x08;
	private static final byte SS_PARTIAL_DEMAND = 0x10;
	private static final byte SS_REVERSE_RUNNING = 0x20;

	private int status;

	public LoadProfileStatus(byte b) {
		this.status = Integer.parseInt(Byte.toString(b), 16);
	}

	public LoadProfileStatus(int i) {
		this.status = i;
	}

	public int getEIStatus() {
		int result = IntervalStateBits.OK;
		if (is(SS_METER_RESET)) {
			result |= IntervalStateBits.OTHER;
		}
		if (is(SS_TIME_CHANGE)) {
			result |= IntervalStateBits.SHORTLONG;
		}
		if (is(SS_OTHER_CHANGE)) {
			result |= IntervalStateBits.CONFIGURATIONCHANGE;
		}
		if (is(SS_PARTIAL_DEMAND)) {
			result |= IntervalStateBits.PHASEFAILURE;
		}
		if (is(SS_REVERSE_RUNNING)) {
			result |= IntervalStateBits.REVERSERUN;
		}
		if (is(SS_BATTERY_MONITOR)) {
			result |= IntervalStateBits.OTHER;
		}
		return result;
	}

	public boolean is(byte statusType) {
		return (this.status & statusType) > 0;
	}

	public String toString() {

		StringBuffer sb = new StringBuffer();

		if (is(SS_METER_RESET)) {
			sb.append("Meter resets which are not power failures ");
		}
		if (is(SS_TIME_CHANGE)) {
			sb.append("Write access which changes time and date ");
		}
		if (is(SS_OTHER_CHANGE)) {
			sb.append("Write access which changes other data ");
		}
		if (is(SS_BATTERY_MONITOR)) {
			sb.append("Battery monitor ");
		}
		if (is(SS_PARTIAL_DEMAND)) {
			sb.append("Partial Demand ");
		}
		if (is(SS_REVERSE_RUNNING)) {
			sb.append("Reverse Running ");
		}

		sb.append(Integer.toBinaryString(this.status));

		return sb.toString();
	}

}