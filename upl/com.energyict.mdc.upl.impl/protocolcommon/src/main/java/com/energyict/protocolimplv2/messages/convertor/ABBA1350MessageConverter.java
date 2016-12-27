package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ABBA1350UserFileMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy ABBA1350 IEC1107 protocol.
 *
 * @author sva
 * @since 25/10/13 - 9:35
 */
public class ABBA1350MessageConverter extends AbstractMessageConverter {

    private static final String CHARSET = "UTF-8";

    private static final String UploadSwitchPointClock = "SPC_DATA";
    private static final String UploadSwitchPointClockUpdate = "SPCU_DATA";

    public ABBA1350MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.SwitchPointClockSettings) || propertySpec.getName().equals(DeviceMessageConstants.SwitchPointClockUpdateSettings)) {
            return this.getExtractor().contents((DeviceMessageFile) messageAttribute, Charset.forName(CHARSET));
        } else {
            return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap.of(
                    messageSpec(ConfigurationChangeDeviceMessage.UploadSwitchPointClockSettings), new ABBA1350UserFileMessageEntry(UploadSwitchPointClock),
                    messageSpec(ConfigurationChangeDeviceMessage.UploadSwitchPointClockUpdateSettings), new ABBA1350UserFileMessageEntry(UploadSwitchPointClockUpdate));
    }
}
