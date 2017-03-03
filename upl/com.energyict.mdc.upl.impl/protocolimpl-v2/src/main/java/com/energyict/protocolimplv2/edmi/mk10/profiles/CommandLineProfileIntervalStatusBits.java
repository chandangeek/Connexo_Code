package com.energyict.protocolimplv2.edmi.mk10.profiles;

import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

/**
 * @author sva
 * @since 27/02/2017 - 15:38
 */
public class CommandLineProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    @Override
    public int getEisStatusCode(int protocolStatusCode) {
        int eiStatus = 0;

        // ABSENT_READING is inverse.
        // Bit is 1 when normal reading
        // Bit is 0 when absent reading
        if ((protocolStatusCode & 0x0001) != 0x0001) {
            eiStatus |= IntervalStateBits.MISSING;
        }
        if ((protocolStatusCode & 0x0002) == 0x0002) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatusCode & 0x0004) == 0x0004) {
            eiStatus |= IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatusCode & 0x0008) == 0x0008) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatusCode & 0x0010) == 0x0010) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatusCode & 0x0020) == 0x0020) {
            eiStatus |= IntervalStateBits.OTHER;
        }

        return eiStatus;
    }
}