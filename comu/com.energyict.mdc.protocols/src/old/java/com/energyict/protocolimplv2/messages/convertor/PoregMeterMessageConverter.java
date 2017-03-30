/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.messages.DSTAlgorithm;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.dstEndAlgorithmAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.dstStartAlgorithmAttributeName;

/**
 * Represents a MessageConverter for the Poreg meter protocols (Poreg2 & Poreg2P)
 *
 * @author khe
 * @since 25/10/13 - 10:46
 */
public class PoregMeterMessageConverter extends AbstractMessageConverter {

    public PoregMeterMessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, new SimpleTagMessageEntry("DemandReset"));
        registry.put(DeviceMessageId.CLOCK_SET_START_OF_DST_WITHOUT_HOUR, new MultipleAttributeMessageEntry("StartOfDST", "Month", "Day of month", "Day of week"));
        registry.put(DeviceMessageId.CLOCK_SET_END_OF_DST_WITHOUT_HOUR, new MultipleAttributeMessageEntry("EndOfDST", "Month", "Day of month", "Day of week"));
        registry.put(DeviceMessageId.CLOCK_SET_DST_ALGORITHM, new MultipleAttributeMessageEntry("Algorithms", "Start Algorithm", "End Algorithm"));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(dstStartAlgorithmAttributeName) || propertySpec.getName().equals(dstEndAlgorithmAttributeName)) {
            return String.valueOf(DSTAlgorithm.fromDescription(messageAttribute.toString()));
        }
        return messageAttribute.toString();
    }

}