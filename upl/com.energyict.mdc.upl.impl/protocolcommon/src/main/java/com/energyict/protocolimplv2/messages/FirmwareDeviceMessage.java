package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>Firmware</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum FirmwareDeviceMessage implements DeviceMessageSpec {

    UPGRADE_FIRMWARE_WITH_USER_FILE(PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName)),
    UPGRADE_FIRMWARE_ACTIVATE(PropertySpecFactory.datePropertySpec(firmwareUpdateActivationDateAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE(PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.datePropertySpec(firmwareUpdateActivationDateAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE(PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.datePropertySpec(firmwareUpdateActivationDateAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateVersionNumberAttributeName)),
    UPGRADE_FIRMWARE_URL(PropertySpecFactory.stringPropertySpec(firmwareUpdateURLAttributeName)),
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE(PropertySpecFactory.stringPropertySpec(firmwareUpdateURLAttributeName),
            PropertySpecFactory.datePropertySpec(firmwareUpdateActivationDateAttributeName)),;

    private static final DeviceMessageCategory firmwareCategory = DeviceMessageCategories.FIRMWARE;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private FirmwareDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return firmwareCategory;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return FirmwareDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}
