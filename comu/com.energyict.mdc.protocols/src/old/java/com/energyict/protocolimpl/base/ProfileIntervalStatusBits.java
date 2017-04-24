/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.interval.IntervalStateBits;

public interface ProfileIntervalStatusBits {

    /**
     * Convert the given protocolStatus code to a proper EIS {@link IntervalStateBits}
     *
     * @param protocolStatusCode the statusCode from the device
     * @return the status code according to the {@link IntervalStateBits}
     */
    public int getEisStatusCode(int protocolStatusCode);

}