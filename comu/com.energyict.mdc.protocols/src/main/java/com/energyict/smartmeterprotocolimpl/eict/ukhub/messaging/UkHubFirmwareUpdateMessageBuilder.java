package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.protocols.messaging.FirmwareUpdateMessageBuilder;

/**
 * Copyrights EnergyICT
 * Date: 22/08/11
 * Time: 14:19
 */
public class UkHubFirmwareUpdateMessageBuilder extends FirmwareUpdateMessageBuilder {

    public static String getMessageNodeTag() {
        return FirmwareUpdateMessageBuilder.getMessageNodeTag();
    }

}
