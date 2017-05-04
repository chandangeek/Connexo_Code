/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1.register;

import com.energyict.mdc.common.interval.IntervalStateBits;

import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

/**
 * can parse a status from a byte, and display itself.
 *
 * @author fbo
 */

public class LoadProfileStatus {

	/* LoadProfileStatus Type */
	public static final byte	SS_DIAGNOSTIC_FLAG	= 0x01;
	public static final byte	SS_WRITE_ACCESS		= 0x02;
	public static final byte	SS_PARTIAL_DEMAND	= 0x04;
	public static final byte	SS_REVERSE_RUN		= 0x08;

	private int status;

	public LoadProfileStatus(byte b) {
		status = b & 0x0F;
	}

	public LoadProfileStatus(int i) {
		this.status = i & 0x0F;
	}

	public int getEIStatus() {
		int result = IntervalStateBits.OK;
		if (is(SS_DIAGNOSTIC_FLAG)) {
			result |= IntervalStateBits.OTHER;
		}
		if (is(SS_WRITE_ACCESS)) {
			result |= IntervalStateBits.CONFIGURATIONCHANGE | IntervalStateBits.SHORTLONG;
		}
		if (is(SS_PARTIAL_DEMAND)) {
			result |= IntervalStateBits.PHASEFAILURE;
		}
		if (is(SS_REVERSE_RUN)) {
			result |= IntervalStateBits.REVERSERUN;
		}
		return result;
	}

	private boolean is(byte statusType) {
		return (this.status & statusType) > 0;
	}

	public String toString() {

		StringBuffer sb = new StringBuffer();

		if (is(SS_DIAGNOSTIC_FLAG)) {
			sb.append("[Diagnostic flag] ");
		}
		if (is(SS_WRITE_ACCESS)) {
			sb.append("[Write access] ");
		}
		if (is(SS_PARTIAL_DEMAND)) {
			sb.append("[Partial Demand] ");
		}
		if (is(SS_REVERSE_RUN)) {
			sb.append("[Reverse Running] ");
		}

		sb.append(Integer.toBinaryString(this.status) + " " + PPMUtils.toHexaString(this.status));

		return sb.toString();
	}

}