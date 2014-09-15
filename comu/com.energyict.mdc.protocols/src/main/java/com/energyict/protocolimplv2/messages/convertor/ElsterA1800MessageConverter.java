package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy Elster A1800 protocol.
 *
 * @author sva
 * @since 25/10/13 - 10:46
 */
public class ElsterA1800MessageConverter extends AbstractMessageConverter {

    private static final String SETLPDIVISOR = "SETLPDIVISOR";
    private static final String CHANNEL = "Channel";
    private static final String DIVISOR = "Divisor";

    public ElsterA1800MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.CHANNEL_CONFIGURATION_SET_LP_DIVISOR, new MultipleAttributeMessageEntry(SETLPDIVISOR, CHANNEL, DIVISOR));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

}