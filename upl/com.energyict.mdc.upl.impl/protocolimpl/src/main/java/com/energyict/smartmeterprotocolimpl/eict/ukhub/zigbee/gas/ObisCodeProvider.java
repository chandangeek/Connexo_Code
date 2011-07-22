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

    public static final ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode FRAUD_DETECTION_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode DISCONNECT_CONTROL_EVENT_LOG = ObisCode.fromString("0.0.99.98.2.255");
    public static final ObisCode FIRMWARE_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");

    public static final ObisCode COMM_FAILURE_EVENT_LOG = ObisCode.fromString("0.0.99.97.6.255");
    public static final ObisCode PREPAYMENT_EVENT_LOG = ObisCode.fromString("0.0.99.97.7.255");

    public static final ObisCode TEXT_MSG_SEND_EVENT_LOG = ObisCode.fromString("0.0.99.97.20.255");
    public static final ObisCode TEXT_MSG_RESPONSE_EVENT_LOG = ObisCode.fromString("0.0.99.97.21.255");

}
