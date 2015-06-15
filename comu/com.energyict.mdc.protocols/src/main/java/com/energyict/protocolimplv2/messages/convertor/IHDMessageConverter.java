package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileMessageEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy IC IHD (In-home display) protocol.
 *
 * @author sva
 * @since 25/10/13 - 9:35
 */
public class IHDMessageConverter extends AbstractMessageConverter {

    /**
     * Default constructor for at-runtime instantiation
     */
    public IHDMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.firmwareUpdateFileAttributeName:
                FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
                return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE, new WebRTUFirmwareUpgradeWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateFileAttributeName));
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE, new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(DeviceMessageConstants.firmwareUpdateFileAttributeName, DeviceMessageConstants.firmwareUpdateActivationDateAttributeName));
        return registry;
    }

}