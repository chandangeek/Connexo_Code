/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.profiles;

import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

/**
 * @author sva
 * @since 27/02/2017 - 15:38
 */
public class MK6ProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    private final int ERROR_READING_REGISTER = 0x0001;
    private final int MISSING_DATA = 0x0002;
    private final int POWER_FAILED_DURING_INTERVAL = 0x0004;
    private final int INCOMPLETE_INTERVAL = 0x0008;
    private final int CALIBRATION_LOST = 0x0020;
    private final int SVFRM_FAILURE = 0x0040;
    private final int EFA_FAILURE_USER_FLAG = 0x0080;
    private final int DATA_CHECKSUM_ERROR = 0x0100;

    @Override
    public int getEisStatusCode(int protocolStatusCode) {
        int eiStatus = 0;

        if ((protocolStatusCode & ERROR_READING_REGISTER) == ERROR_READING_REGISTER) {
            eiStatus |= IntervalStateBits.CORRUPTED;
        }
        if ((protocolStatusCode & MISSING_DATA) == MISSING_DATA) {
            eiStatus |= IntervalStateBits.MISSING;
        }
        if ((protocolStatusCode & POWER_FAILED_DURING_INTERVAL) == POWER_FAILED_DURING_INTERVAL) {
            eiStatus |= IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatusCode & INCOMPLETE_INTERVAL) == INCOMPLETE_INTERVAL) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatusCode & CALIBRATION_LOST) == CALIBRATION_LOST) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatusCode & SVFRM_FAILURE) == SVFRM_FAILURE) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatusCode & EFA_FAILURE_USER_FLAG) == EFA_FAILURE_USER_FLAG) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatusCode & DATA_CHECKSUM_ERROR) == DATA_CHECKSUM_ERROR) {
            eiStatus |= IntervalStateBits.CORRUPTED;
        }

        return eiStatus;
    }
}