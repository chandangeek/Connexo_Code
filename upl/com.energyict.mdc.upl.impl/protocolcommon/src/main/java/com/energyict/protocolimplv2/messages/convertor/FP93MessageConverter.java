package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.TotalizersConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Represents a MessageConverter for the FP93 protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class FP93MessageConverter extends AbstractMessageConverter {

    public FP93MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap.of(
                messageSpec(ConfigurationChangeDeviceMessage.Clear_Faults_Flags), new SimpleTagMessageEntry("Clear_Faults_Flags"),
                messageSpec(ConfigurationChangeDeviceMessage.Clear_Statistical_Values), new SimpleTagMessageEntry("Clear_Statistical_Values"),
                messageSpec(TotalizersConfigurationDeviceMessage.ClearTotalizers), new SimpleTagMessageEntry("Clear_Totalizers"));
    }
}