package com.energyict.smartmeterprotocolimpl.iskra.mt880.profiles;


import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

/**
 * @author sva
 * @since 11/10/13 - 17:10
 */
public class MT880ProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    static final int CRITICAL_ERROR = 0x01;
    static final int CLOCK_INVALID = 0x02;
    static final int DATA_NOT_VALID = 0x04;
    static final int DAYLIGHT_SAVING = 0x08;
    static final int BILLING_RESET = 0x10;
    static final int CLOCK_ADJUSTED = 0x20;
    static final int POWER_UP = 0x40;
    static final int POWER_DOWN = 0x80;
    static final int POWER_DOWN_L1 = 0x100;
    static final int POWER_DOWN_L2 = 0x200;
    static final int POWER_DOWN_L3 = 0x400;
    static final int POWER_UP_L1 = 0x800;
    static final int POWER_UP_L2 = 0x1000;
    static final int POWER_UP_L3 = 0x2000;
    static final int PARAMETER_CHANGED = 0x4000;
    static final int CLOCK_SYNCHRONIZED = 0x8000;

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
        if ((statusCodeProfile & BILLING_RESET) == BILLING_RESET) {
            eiCode |= IntervalStateBits.OTHER;
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
        if ((statusCodeProfile & POWER_DOWN_L1) == POWER_DOWN_L1) {
            eiCode |= IntervalStateBits.POWERDOWN;
        }
        if ((statusCodeProfile & POWER_DOWN_L2) == POWER_DOWN_L2) {
            eiCode |= IntervalStateBits.POWERDOWN;
        }
        if ((statusCodeProfile & POWER_DOWN_L3) == POWER_DOWN_L3) {
            eiCode |= IntervalStateBits.POWERDOWN;
        }
        if ((statusCodeProfile & POWER_UP_L1) == POWER_UP_L1) {
            eiCode |= IntervalStateBits.POWERUP;
        }
        if ((statusCodeProfile & POWER_UP_L2) == POWER_UP_L2) {
            eiCode |= IntervalStateBits.POWERUP;
        }
        if ((statusCodeProfile & POWER_UP_L3) == POWER_UP_L3) {
            eiCode |= IntervalStateBits.POWERUP;
        }
        if ((statusCodeProfile & PARAMETER_CHANGED) == PARAMETER_CHANGED) {
            eiCode |= IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if ((statusCodeProfile & CLOCK_SYNCHRONIZED) == CLOCK_SYNCHRONIZED) {
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        return eiCode;
    }
}
