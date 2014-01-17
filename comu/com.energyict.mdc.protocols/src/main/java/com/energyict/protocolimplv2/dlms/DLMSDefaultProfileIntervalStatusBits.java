package com.energyict.protocolimplv2.dlms;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

/**
 * Straightforward implementation which just returns the given statusCode back to the profileBuilder
 *
 * <pre>
 * Copyrights EnergyICT
 * Date: 11-mrt-2011
 * Time: 9:02:41
 * </pre>
 */
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
