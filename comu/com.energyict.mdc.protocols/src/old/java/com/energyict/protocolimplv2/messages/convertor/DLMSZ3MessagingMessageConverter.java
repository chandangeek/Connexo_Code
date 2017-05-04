/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activateNowAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.invertDigitalOutput1AttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.invertDigitalOutput2AttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.readFrequencyInMinutesAttributeName;

/**
 * Represents a MessageConverter for the legacy DLMSZ3Messaging protocol.
 *
 * @author khe
 * @since 25/10/13 - 10:46
 */
public class DLMSZ3MessagingMessageConverter extends AbstractMessageConverter {

    public DLMSZ3MessagingMessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, new MultipleAttributeMessageEntry(RtuMessageConstant.CONNECT_LOAD, RtuMessageConstant.DIGITAL_OUTPUT));
        registry.put(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, new MultipleAttributeMessageEntry(RtuMessageConstant.DISCONNECT_LOAD, RtuMessageConstant.DIGITAL_OUTPUT));

        registry.put(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3, new MultipleAttributeMessageEntry("Configure_load_limiting", "Read_frequency", "Threshold", "Duration", "Digital_Output1_Invert", "Digital_Output2_Invert", "Activate_now"));
        registry.put(DeviceMessageId.LOAD_BALANCING_ENABLE_LOAD_LIMITING, new OneTagMessageEntry("Enable_load_limiting"));
        registry.put(DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING, new OneTagMessageEntry("Disable_load_limitng"));

        registry.put(DeviceMessageId.PREPAID_CONFIGURATION_ADD_CREDIT, new MultipleAttributeMessageEntry("Add_Prepaid_credit", "Budget"));
        //TODO add message to configure prepaid, this uses optional attributes!
        registry.put(DeviceMessageId.PREPAID_CONFIGURATION_ENABLE, new MultipleAttributeMessageEntry("Disable_Prepaid_functionality"));
        registry.put(DeviceMessageId.PREPAID_CONFIGURATION_DISABLE, new OneTagMessageEntry("Disable_Prepaid_functionality"));
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