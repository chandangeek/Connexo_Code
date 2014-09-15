package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy ABBA1700 protocol.
 *
 *  @author sva
  * @since 24/10/13 - 10:10
 */
public class ABBA1700MessageConverter extends AbstractMessageConverter {

    private static final String DEMAND_RESET = "DemandReset";

    /**
     * Represents a mapping between {@link DeviceMessageSpec}s
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET, new OneTagMessageEntry(DEMAND_RESET));
    }

    public ABBA1700MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

}