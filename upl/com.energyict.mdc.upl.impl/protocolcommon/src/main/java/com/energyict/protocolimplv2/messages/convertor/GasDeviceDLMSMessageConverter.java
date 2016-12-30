package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetMBusEncryptionKeysMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.openKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.transferKeyAttributeName;

/**
 * Represents a MessageConverter for the DLMS AS220 GasDevice protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class GasDeviceDLMSMessageConverter extends AbstractMessageConverter {

    public GasDeviceDLMSMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor, deviceExtractor, registerExtractor, loadProfileExtractor, numberLookupExtractor, deviceMessageFileExtractor, tariffCalendarExtractor);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(openKeyAttributeName) || propertySpec.getName().equals(transferKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        }

        return messageAttribute.toString();//Works for hex, string and bigdecimal
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new SimpleTagMessageEntry("DisconnectGmeter"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new SimpleTagMessageEntry("ConnectGmeter"))
                .put(messageSpec(MBusSetupDeviceMessage.Decommission), new SimpleTagMessageEntry("Decommission"))
                .put(messageSpec(MBusSetupDeviceMessage.SetEncryptionKeys), new SetMBusEncryptionKeysMessageEntry(openKeyAttributeName, transferKeyAttributeName, "EnableEncryption"))
                .put(messageSpec(MBusSetupDeviceMessage.WriteCaptureDefinition), new MultipleAttributeMessageEntry("WriteCaptureDefinition", "DIB", "VIB"))
                .build();
    }
}