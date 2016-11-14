package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.enums.DSTAlgorithm;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dstEndAlgorithmAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dstStartAlgorithmAttributeName;

/**
 * Represents a MessageConverter for the Poreg meter protocols (Poreg2 & Poreg2P)
 *
 * @author khe
 * @since 25/10/13 - 10:46
 */
public class PoregMeterMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(DeviceActionMessage.DEMAND_RESET, new SimpleTagMessageEntry("DemandReset"));
        registry.put(ClockDeviceMessage.SetStartOfDSTWithoutHour, new MultipleAttributeMessageEntry("StartOfDST", "Month", "Day of month", "Day of week"));
        registry.put(ClockDeviceMessage.SetEndOfDSTWithoutHour, new MultipleAttributeMessageEntry("EndOfDST", "Month", "Day of month", "Day of week"));
        registry.put(ClockDeviceMessage.SetDSTAlgorithm, new MultipleAttributeMessageEntry("Algorithms", "Start Algorithm", "End Algorithm"));
    }

    public PoregMeterMessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
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