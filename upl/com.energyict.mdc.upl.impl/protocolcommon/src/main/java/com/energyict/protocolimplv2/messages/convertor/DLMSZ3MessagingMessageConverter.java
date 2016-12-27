package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.PrepaidConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
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

    public DLMSZ3MessagingMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT), new MultipleAttributeMessageEntry(RtuMessageConstant.CONNECT_LOAD, RtuMessageConstant.DIGITAL_OUTPUT))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT), new MultipleAttributeMessageEntry(RtuMessageConstant.DISCONNECT_LOAD, RtuMessageConstant.DIGITAL_OUTPUT))

                .put(messageSpec(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3), new MultipleAttributeMessageEntry("Configure_load_limiting", "Read_frequency", "Threshold", "Duration", "Digital_Output1_Invert", "Digital_Output2_Invert", "Activate_now"))
                .put(messageSpec(LoadBalanceDeviceMessage.ENABLE_LOAD_LIMITING), new OneTagMessageEntry("Enable_load_limiting"))
                .put(messageSpec(LoadBalanceDeviceMessage.DISABLE_LOAD_LIMITING), new OneTagMessageEntry("Disable_load_limitng"))

                .put(messageSpec(PrepaidConfigurationDeviceMessage.AddPrepaidCredit), new MultipleAttributeMessageEntry("Add_Prepaid_credit", "Budget"))
                //TODO add message to configure prepaid, this uses optional attributes!
                .put(messageSpec(PrepaidConfigurationDeviceMessage.EnablePrepaid), new MultipleAttributeMessageEntry("Disable_Prepaid_functionality"))
                .put(messageSpec(PrepaidConfigurationDeviceMessage.DisablePrepaid), new OneTagMessageEntry("Disable_Prepaid_functionality"))
                .build();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(readFrequencyInMinutesAttributeName)) {
            return String.valueOf(((Duration) messageAttribute).getSeconds() / 60);
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)) {
            return String.valueOf(Temporals.toSeconds((TemporalAmount) messageAttribute));
        } else if (propertySpec.getName().equals(invertDigitalOutput1AttributeName)
                || propertySpec.getName().equals(invertDigitalOutput2AttributeName)
                || propertySpec.getName().equals(activateNowAttributeName)) {
            return ((Boolean) messageAttribute) ? "1" : "0";     //Protocol expects "1" for true, or "0" for false
        }
        return messageAttribute.toString();     //E.g. BigDecimal = "111", Boolean = "true" or "false", ...
    }
}