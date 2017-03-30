/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms;

import com.energyict.mdc.common.interval.IntervalStateBits;

import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

public class DLMSDefaultProfileIntervalStatusBits implements ProfileIntervalStatusBits{

    /**
     * Convert the given protocolStatus code to a proper EIS {@link IntervalStateBits}
     *
     * @param protocolStatusCode the statusCode from the device
     * @return the status code according to the {@link IntervalStateBits}
     */
    public int getEisStatusCode(int protocolStatusCode) {
        return protocolStatusCode;
    }
}
