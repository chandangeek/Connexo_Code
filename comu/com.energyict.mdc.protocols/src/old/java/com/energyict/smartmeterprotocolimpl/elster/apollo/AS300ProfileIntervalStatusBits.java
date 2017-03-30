/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.mdc.common.interval.IntervalStateBits;

import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

import java.util.HashMap;
import java.util.Map;

public class AS300ProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    /**
     * This map contains the mapping between the meter interval status flags and the EIServer interval state flags
     */
    private static final Map<Integer, Integer> STATUS_MAPPING = new HashMap<Integer, Integer>();

    static {
        STATUS_MAPPING.put(0x01, IntervalStateBits.DEVICE_ERROR);   // Critical error
        STATUS_MAPPING.put(0x02, IntervalStateBits.BADTIME);        // Clock invalid
        STATUS_MAPPING.put(0x04, IntervalStateBits.CORRUPTED);      // Data not valid
        STATUS_MAPPING.put(0x08, IntervalStateBits.OK);             // DST
        STATUS_MAPPING.put(0x10, IntervalStateBits.MISSING);        // Profile gap
        STATUS_MAPPING.put(0x20, IntervalStateBits.SHORTLONG);      // Clock adjusted
        STATUS_MAPPING.put(0x40, IntervalStateBits.OTHER);          // Asynchronous billing reset
        STATUS_MAPPING.put(0x80, IntervalStateBits.POWERDOWN);      // Power down
    }

    /**
     * Convert the given protocolStatus code to a proper EIS {@link IntervalStateBits}
     *
     * @param protocolStatusCode the statusCode from the device
     * @return the status code according to the {@link IntervalStateBits}
     */
    public int getEisStatusCode(int protocolStatusCode) {
        int eiCode = 0;
        for (Integer status : STATUS_MAPPING.keySet()) {
            if ((protocolStatusCode & status) != 0) {
                eiCode |= STATUS_MAPPING.get(status);
            }
        }
        return eiCode;
    }

}
