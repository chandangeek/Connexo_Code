package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 7/01/2015 - 15:24
 */
public class KaifaDsmr40MessageConverter extends Dsmr40MessageConverter {

    static {
        registry.put(MBusSetupDeviceMessage.Reset_MBus_Client, new MultipleAttributeMessageEntry(RtuMessageConstant.RESET_MBUS_CLIENT, RtuMessageConstant.MBUS_SERIAL_NUMBER));
    }
}
