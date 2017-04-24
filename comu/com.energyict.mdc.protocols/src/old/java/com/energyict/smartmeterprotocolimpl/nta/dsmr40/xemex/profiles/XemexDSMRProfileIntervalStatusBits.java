/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.profiles;

import com.energyict.mdc.common.interval.IntervalStateBits;

import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

public class XemexDSMRProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    static final int CRITICAL_ERROR = 0x01;
    static final int CLOCK_INVALID = 0x02;
    static final int DATA_NOT_VALID = 0x04;
    static final int DAYLIGHT_SAVING = 0x08;
    static final int BILLING_RESET = 0x10;  // only used by IskraEmeko for the billing reset
    static final int CLOCK_ADJUSTED = 0x20;
    static final int POWER_UP = 0x40;  // only used by IskraEmeko for power return
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
//        if ((statusCodeProfile & DAYLIGHT_SAVING) == DAYLIGHT_SAVING) {      -- Do NOT create an intervalStateBit for DST
//            eiCode |= IntervalStateBits.OTHER;
//        }
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

        return eiCode;
    }
}