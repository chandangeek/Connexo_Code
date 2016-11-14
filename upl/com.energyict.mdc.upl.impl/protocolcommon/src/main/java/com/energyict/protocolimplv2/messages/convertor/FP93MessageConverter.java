package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.TotalizersConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the FP93 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class FP93MessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ConfigurationChangeDeviceMessage.Clear_Faults_Flags, new SimpleTagMessageEntry("Clear_Faults_Flags"));
        registry.put(ConfigurationChangeDeviceMessage.Clear_Statistical_Values, new SimpleTagMessageEntry("Clear_Statistical_Values"));
        registry.put(TotalizersConfigurationDeviceMessage.ClearTotalizers, new SimpleTagMessageEntry("Clear_Totalizers"));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public FP93MessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}