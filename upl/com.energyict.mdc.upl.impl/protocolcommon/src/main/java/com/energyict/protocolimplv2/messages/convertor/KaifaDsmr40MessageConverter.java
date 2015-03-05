package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 7/01/2015 - 15:24
 */
public class KaifaDsmr40MessageConverter extends Dsmr40MessageConverter {

    protected static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(Dsmr40MessageConverter.registry);

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    static {
        registry.put(MBusSetupDeviceMessage.Reset_MBus_Client, new MultipleAttributeMessageEntry(RtuMessageConstant.RESET_MBUS_CLIENT, RtuMessageConstant.MBUS_SERIAL_NUMBER));
    }
}
