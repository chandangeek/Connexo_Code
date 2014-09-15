package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
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
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                return Integer.toString(((UserFile) messageAttribute).getId());
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE, new WebRTUFirmwareUpgradeWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName));
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE, new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName, DeviceMessageConstants.firmwareUpdateActivationDateAttributeName));
        return registry;
    }

}