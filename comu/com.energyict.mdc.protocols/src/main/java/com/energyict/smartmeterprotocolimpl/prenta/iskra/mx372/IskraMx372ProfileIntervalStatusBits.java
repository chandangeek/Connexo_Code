package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 24/01/12
 * Time: 9:09
 */
public class IskraMx372ProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    static final int DEVICE_ERROR = 0x01;   // PROFILE_STATUS_DEVICE_DISTURBANCE
    static final int OTHER = 0x10;          // PROFILE_STATUS_RESET_CUMULATION
    static final int SHORT_LONG = 0x04;     // PROFILE_STATUS_DEVICE_CLOCK_CHANGED
    static final int POWER_UP = 0x40;       // PROFILE_STATUS_POWER_RETURNED
    static final int POWER_DOWN = 0x80;     // PROFILE_STATUS_POWER_FAILURE

    /**
     * Convert the given protocolStatus code to a proper EIS IntervalStateBits
     *
     * @param protocolStatusCode the statusCode from the device
     * @return the status code according to the IntervalStateBits
     **/
    public int getEisStatusCode(int protocolStatusCode) {
        int eiCode = 0;

        if ((protocolStatusCode & DEVICE_ERROR) == DEVICE_ERROR) {
            eiCode |= IntervalStateBits.DEVICE_ERROR;
        }
        if ((protocolStatusCode & OTHER) == OTHER) {
            eiCode |= IntervalStateBits.OTHER;
        }
        if ((protocolStatusCode & SHORT_LONG) == SHORT_LONG) {
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatusCode & POWER_UP) == POWER_UP) {
            eiCode |= IntervalStateBits.POWERUP;
        }
        if ((protocolStatusCode & POWER_DOWN) == POWER_DOWN) {
            eiCode |= IntervalStateBits.POWERDOWN;
        }
        return eiCode;
    }
}
