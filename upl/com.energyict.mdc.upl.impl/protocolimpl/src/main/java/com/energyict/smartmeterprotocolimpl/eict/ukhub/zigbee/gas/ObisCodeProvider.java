package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas;

import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 17:09
 */
public final class ObisCodeProvider {

    /**
     * Util class, so made constructor private
     */
    private ObisCodeProvider() {
        // Nothing to do here
    }

    public static final ObisCode FIRMWARE_VERSION_MID = ObisCode.fromString("7.0.0.2.1.255");
    public static final ObisCode SERIAL_NUMBER = ObisCode.fromString("0.0.96.1.0.255");

}
