/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.mdc.common.interval.IntervalStateBits;

/**
 * OverView of the
 *
 * @author gna
 * @since 17-feb-2010
 *
 */
public final class GasStatusCodes {

    /** Creates a new instance of StatusCodeProfile */
    private GasStatusCodes() {
    }


    static final int APPLICATION_ERROR1=0x01;
    static final int APPLICATION_ERROR2=0x02;
    static final int BATTERY_LOW=0x04;
    static final int PERMANENT_ERROR=0x08;
    static final int TEMPORARY_ERROR=0x10;
    static final int CLOCK_INVALID=0x20;	// more then 60 seconds
    static final int FRAUD_ATTEMPT=0x40;
    static final int VALVE_ALARM=0x80;

    public static int intervalStateBits(int statusCodeProfile) {

        int eiCode=0;

        int firstByte = (statusCodeProfile >> 8)&0x00FF;	// Contains valve open and Meter valid
        int secondByte = (statusCodeProfile >> 0)&0x00FF;

        if ((secondByte & APPLICATION_ERROR1) == APPLICATION_ERROR1) {
			eiCode |= IntervalStateBits.CORRUPTED;
		}
        if ((secondByte & APPLICATION_ERROR2) == APPLICATION_ERROR2) {
			eiCode |= IntervalStateBits.CORRUPTED;
		}
        if ((secondByte & BATTERY_LOW) == BATTERY_LOW) {
			eiCode |= IntervalStateBits.BATTERY_LOW;
		}
        if ((secondByte & PERMANENT_ERROR) == PERMANENT_ERROR) {
			eiCode |= IntervalStateBits.OTHER;
		}
        if ((secondByte & TEMPORARY_ERROR) == TEMPORARY_ERROR) {
			eiCode |= IntervalStateBits.MISSING;
		}
        if ((secondByte & CLOCK_INVALID) == CLOCK_INVALID) {
			eiCode |= IntervalStateBits.BADTIME;
		}
        if ((secondByte & FRAUD_ATTEMPT) == FRAUD_ATTEMPT) {
			eiCode |= IntervalStateBits.OTHER;
		}
        if ((secondByte & VALVE_ALARM) == VALVE_ALARM) {
			eiCode |= IntervalStateBits.OTHER;
		}

        return eiCode;
    }
}
