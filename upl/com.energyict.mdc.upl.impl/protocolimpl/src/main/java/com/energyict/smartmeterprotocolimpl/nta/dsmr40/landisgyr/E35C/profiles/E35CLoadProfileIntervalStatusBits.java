package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.profiles;

import com.energyict.protocol.IntervalStateBits;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits;

/**
 * @author sva
 * @since 1/07/2015 - 15:33
 */
public class E35CLoadProfileIntervalStatusBits extends DSMRProfileIntervalStatusBits {

    static final int CRITICAL_ERROR = 0x01;
    static final int CLOCK_INVALID = 0x02;
    static final int DATA_NOT_VALID = 0x04;
    static final int DAYLIGHT_SAVING = 0x08;
    static final int NO_DATA = 0x10;
    static final int CLOCK_ADJUSTED = 0x20;
    static final int POWER_UP = 0x40;
    static final int POWER_DOWN = 0x80;

    public int getEisStatusCode(final int statusCodeProfile) {
        int eiCode = 0;

        if ((statusCodeProfile & CRITICAL_ERROR) == CRITICAL_ERROR) {
            eiCode |= IntervalStateBits.DEVICE_ERROR;
        }
        if ((statusCodeProfile & CLOCK_INVALID) == CLOCK_INVALID) {
            eiCode |= IntervalStateBits.BADTIME;
        }
        if ((statusCodeProfile & DATA_NOT_VALID) == DATA_NOT_VALID) {
            eiCode |= IntervalStateBits.CORRUPTED;
        }
        if ((statusCodeProfile & DAYLIGHT_SAVING) == DAYLIGHT_SAVING) {
            eiCode |= IntervalStateBits.OTHER;
        }
        if ((statusCodeProfile & NO_DATA) == NO_DATA) {
            eiCode |= IntervalStateBits.MISSING;
        }
        if ((statusCodeProfile & CLOCK_ADJUSTED) == CLOCK_ADJUSTED) {
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        if ((statusCodeProfile & POWER_UP) == POWER_UP) {
            eiCode |= IntervalStateBits.POWERUP;
        }
        if ((statusCodeProfile & POWER_DOWN) == POWER_DOWN) {
            eiCode |= IntervalStateBits.POWERDOWN;
        }

        return eiCode;
    }
}
