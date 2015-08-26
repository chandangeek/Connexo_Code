package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cisac on 8/13/2015.
 */
public class IHDAM110RMessageConverter extends AbstractMessageConverter{
    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE,
                new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE));

        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE,
                new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE, RtuMessageConstant.ACTIVATE_DATE));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public IHDAM110RMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                return new String(((UserFile) messageAttribute).loadFileInByteArray(), Charset.forName("UTF-8"));   // We suppose the UserFile contains regular ASCII
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
