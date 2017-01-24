package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.BillingResetMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy ABBA1140 protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 12:03
 */
public class ABBA1140MessageConverter extends AbstractMessageConverter {

    public ABBA1140MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET, new BillingResetMessageEntry());
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

}