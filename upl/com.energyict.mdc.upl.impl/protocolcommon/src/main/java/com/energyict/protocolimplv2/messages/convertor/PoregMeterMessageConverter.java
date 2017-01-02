package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.enums.DSTAlgorithm;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dstEndAlgorithmAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dstStartAlgorithmAttributeName;

/**
 * Represents a MessageConverter for the Poreg meter protocols (Poreg2 & Poreg2P).
 *
 * @author khe
 * @since 25/10/13 - 10:46
 */
public class PoregMeterMessageConverter extends AbstractMessageConverter {

    protected PoregMeterMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
    }

}

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                .put(messageSpec(DeviceActionMessage.DEMAND_RESET), new SimpleTagMessageEntry("DemandReset"))
                .put(messageSpec(ClockDeviceMessage.SetStartOfDSTWithoutHour), new MultipleAttributeMessageEntry("StartOfDST", "Month", "Day of month", "Day of week"))
                .put(messageSpec(ClockDeviceMessage.SetEndOfDSTWithoutHour), new MultipleAttributeMessageEntry("EndOfDST", "Month", "Day of month", "Day of week"))
                .put(messageSpec(ClockDeviceMessage.SetDSTAlgorithm), new MultipleAttributeMessageEntry("Algorithms", "Start Algorithm", "End Algorithm"))
                .build();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(dstStartAlgorithmAttributeName) || propertySpec.getName().equals(dstEndAlgorithmAttributeName)) {
            return String.valueOf(DSTAlgorithm.fromDescription(messageAttribute.toString()));
        }
        return messageAttribute.toString();
    }

}