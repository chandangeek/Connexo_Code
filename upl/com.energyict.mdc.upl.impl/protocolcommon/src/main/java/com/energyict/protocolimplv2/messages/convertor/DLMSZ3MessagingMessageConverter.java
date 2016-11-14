package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.PrepaidConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activateNowAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.invertDigitalOutput1AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.invertDigitalOutput2AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.readFrequencyInMinutesAttributeName;

/**
 * Represents a MessageConverter for the legacy DLMSZ3Messaging protocol.
 *
 * @author khe
 * @since 25/10/13 - 10:46
 */
public class DLMSZ3MessagingMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT, new MultipleAttributeMessageEntry(RtuMessageConstant.CONNECT_LOAD, RtuMessageConstant.DIGITAL_OUTPUT));
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT, new MultipleAttributeMessageEntry(RtuMessageConstant.DISCONNECT_LOAD, RtuMessageConstant.DIGITAL_OUTPUT));

        registry.put(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3, new MultipleAttributeMessageEntry("Configure_load_limiting", "Read_frequency", "Threshold", "Duration", "Digital_Output1_Invert", "Digital_Output2_Invert", "Activate_now"));
        registry.put(LoadBalanceDeviceMessage.ENABLE_LOAD_LIMITING, new OneTagMessageEntry("Enable_load_limiting"));
        registry.put(LoadBalanceDeviceMessage.DISABLE_LOAD_LIMITING, new OneTagMessageEntry("Disable_load_limitng"));

        registry.put(PrepaidConfigurationDeviceMessage.AddPrepaidCredit, new MultipleAttributeMessageEntry("Add_Prepaid_credit", "Budget"));
        //TODO add message to configure prepaid, this uses optional attributes!
        registry.put(PrepaidConfigurationDeviceMessage.EnablePrepaid, new MultipleAttributeMessageEntry("Disable_Prepaid_functionality"));
        registry.put(PrepaidConfigurationDeviceMessage.DisablePrepaid, new OneTagMessageEntry("Disable_Prepaid_functionality"));
    }

    public DLMSZ3MessagingMessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(readFrequencyInMinutesAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds() / 60);
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(invertDigitalOutput1AttributeName)
                || propertySpec.getName().equals(invertDigitalOutput2AttributeName)
                || propertySpec.getName().equals(activateNowAttributeName)) {
            return ((Boolean) messageAttribute) ? "1" : "0";     //Protocol expects "1" for true, or "0" for false
        }
        return messageAttribute.toString();     //E.g. BigDecimal = "111", Boolean = "true" or "false", ...
    }
}