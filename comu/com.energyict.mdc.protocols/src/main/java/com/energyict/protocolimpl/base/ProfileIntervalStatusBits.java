package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.interval.IntervalStateBits;

/**
 * Each {@link com.energyict.mdc.protocol.api.device.data.ProfileData} contains intervals with a status code.
 * Depending on the protocol different interpretations are given to the statusFlags.
 * This interface generalizes the conversion of the intervalStateBits.
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 8-dec-2010<br/>
 * Time: 9:22:08<br/>
 */
public interface ProfileIntervalStatusBits {

    /**
     * Convert the given protocolStatus code to a proper EIS {@link IntervalStateBits}
     *
     * @param protocolStatusCode the statusCode from the device
     * @return the status code according to the {@link IntervalStateBits}
     */
    public int getEisStatusCode(int protocolStatusCode);

}