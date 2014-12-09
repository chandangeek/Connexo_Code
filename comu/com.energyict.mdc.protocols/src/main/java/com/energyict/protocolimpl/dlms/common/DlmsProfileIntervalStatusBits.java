package com.energyict.protocolimpl.dlms.common;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

/**
 * Copyrights EnergyICT
 * Date: 8-dec-2010
 * Time: 9:34:57
 */
public class DlmsProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    /**
     * This bit is set to indicate that a power failure has occurred
     */
    public static final int PowerDown = 0x02;
    /**
     * This bit is set to indicate that someone tried ot connect ot the meter without permission
     */
    public static final int IntrusionDetection = 0x04;
    /**
     * Indicates some parameter has been modified
     */
    public static final int ParametersChanged = 0x08;
    /**
     * the bit is set when clock has been adjusted less than the synchronisation limit
     */
    public static final int ClockVerified = 0x10;
    /**
     * The bit is set when the value of the register overloads its limit. It is considered as RES(not used) in loadProfile 1
     */
    public static final int OverFlow = 0x20;
    /**
     * The bit is set when clock has been adjusted more or equal than the synchronisation limit
     */
    public static final int ClockAdjusted = 0x40;

    /**
     * Convert the given protocolStatus code to a proper EIS {@link com.energyict.protocol.IntervalStateBits}
     *
     * @param protocolStatusCode the statusCode from the device
     * @return the status code according to the {@link com.energyict.protocol.IntervalStateBits}
     */
    public int getEisStatusCode(int protocolStatusCode) {
        int eiCode = 0;

        if ((protocolStatusCode & PowerDown) == PowerDown) {
            eiCode |= IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatusCode & IntrusionDetection) == IntrusionDetection) {
            eiCode |= IntervalStateBits.OTHER; // because we don't have an IntrusionDetection yet
        }
        if ((protocolStatusCode & ParametersChanged) == ParametersChanged) {
            eiCode |= IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if ((protocolStatusCode & ClockVerified) == ClockVerified) {
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatusCode & OverFlow) == OverFlow) {
            eiCode |= IntervalStateBits.OVERFLOW;
        }
        if ((protocolStatusCode & ClockAdjusted) == ClockAdjusted) {
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        return eiCode;
    }
}
