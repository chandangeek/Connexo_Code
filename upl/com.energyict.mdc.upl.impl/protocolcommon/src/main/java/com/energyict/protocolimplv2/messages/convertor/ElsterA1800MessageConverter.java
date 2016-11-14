package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.ChannelConfigurationDeviceMessage;
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

    /**
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ChannelConfigurationDeviceMessage.SetLPDivisor, new MultipleAttributeMessageEntry(SETLPDIVISOR, CHANNEL, DIVISOR));
    }

    public ElsterA1800MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }
}