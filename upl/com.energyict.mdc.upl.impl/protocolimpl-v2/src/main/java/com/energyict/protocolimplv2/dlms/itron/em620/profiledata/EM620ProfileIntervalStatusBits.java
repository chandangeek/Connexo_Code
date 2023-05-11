/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.itron.em620.profiledata;

import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

public class EM620ProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    static final int CRITICAL_ERROR = 0x01;
    static final int CLOCK_INVALID = 0x02;
    static final int DATA_NOT_VALID = 0x04;
    static final int DAYLIGHT_SAVING = 0x08;
    static final int CONFIGURATION_UPDATED = 0x10;
    static final int CLOCK_ADJUSTED = 0x20;
    static final int OVERFLOW = 0x40;
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
        if ((statusCodeProfile & CONFIGURATION_UPDATED) == CONFIGURATION_UPDATED) {
            eiCode |= IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if ((statusCodeProfile & CLOCK_ADJUSTED) == CLOCK_ADJUSTED) {
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        if ((statusCodeProfile & OVERFLOW) == OVERFLOW) {
            eiCode |= IntervalStateBits.OVERFLOW;
        }
        if ((statusCodeProfile & POWER_DOWN) == POWER_DOWN) {
            eiCode |= IntervalStateBits.POWERDOWN;
        }
        return eiCode;
    }
}
