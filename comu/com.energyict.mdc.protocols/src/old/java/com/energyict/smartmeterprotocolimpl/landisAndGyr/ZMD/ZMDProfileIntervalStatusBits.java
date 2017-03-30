/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.mdc.common.interval.IntervalStateBits;

import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;


public class ZMDProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    static final int CORRUPTED_MEASUREMENT = 0x04;
    static final int TIME_DATE_ADJUSTED = 0x20;
    static final int POWER_UP = 0x40;
    static final int POWER_DOWN = 0x80;

    public int getEisStatusCode(final int statusCodeProfile) {
        int eiCode = 0;

        if ((statusCodeProfile & CORRUPTED_MEASUREMENT) == CORRUPTED_MEASUREMENT) {
            eiCode |= IntervalStateBits.CORRUPTED;
        }
        if ((statusCodeProfile & TIME_DATE_ADJUSTED) == TIME_DATE_ADJUSTED) {
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