package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy Iskra ME37X protocol.
 *
 * @author khe
 * @since 25/10/13 - 10:46
 */
public class IskraME37XMessageConverter extends AbstractMessageConverter {

    public IskraME37XMessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new SimpleTagMessageEntry("CONNECT"));
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new SimpleTagMessageEntry("DISCONNECT"));
        registry.put(DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, new SimpleTagMessageEntry(RtuMessageConstant.DEMAND_RESET, false));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

}