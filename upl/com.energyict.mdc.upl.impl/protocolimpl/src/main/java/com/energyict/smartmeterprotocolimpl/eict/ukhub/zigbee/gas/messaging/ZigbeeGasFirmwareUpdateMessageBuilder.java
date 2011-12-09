package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging;

import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;

/**
 * Copyrights EnergyICT
 * Date: 22/08/11
 * Time: 14:19
 */
public class ZigbeeGasFirmwareUpdateMessageBuilder extends FirmwareUpdateMessageBuilder {

    public static String getMessageNodeTag() {
        return FirmwareUpdateMessageBuilder.getMessageNodeTag();
    }

}
