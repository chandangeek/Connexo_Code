package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateURLAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateVersionNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;

/**
 * Provides a summary of all <i>Firmware</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum FirmwareDeviceMessage implements DeviceMessageSpec {

    UPGRADE_FIRMWARE_WITH_USER_FILE(
            RequiredPropertySpecFactory.newInstance().
                    referencePropertySpec(
                            firmwareUpdateUserFileAttributeName,
                            (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id()))),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION(
            RequiredPropertySpecFactory.newInstance().
                    referencePropertySpec(
                            firmwareUpdateUserFileAttributeName,
                            (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id())),
            RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(resumeFirmwareUpdateAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE(
            RequiredPropertySpecFactory.newInstance().
                    referencePropertySpec(
                            firmwareUpdateUserFileAttributeName,
                            (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id())),
            RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(resumeFirmwareUpdateAttributeName),
            RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(plcTypeFirmwareUpdateAttributeName)),
    UPGRADE_FIRMWARE_ACTIVATE(RequiredPropertySpecFactory.newInstance().datePropertySpec(firmwareUpdateActivationDateAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE(
            RequiredPropertySpecFactory.newInstance().
                    referencePropertySpec(
                            firmwareUpdateUserFileAttributeName,
                            (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id())),
            RequiredPropertySpecFactory.newInstance().datePropertySpec(firmwareUpdateActivationDateAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE(
            RequiredPropertySpecFactory.newInstance().
                    referencePropertySpec(
                            firmwareUpdateUserFileAttributeName,
                            (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id())),
            RequiredPropertySpecFactory.newInstance().datePropertySpec(firmwareUpdateActivationDateAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(firmwareUpdateVersionNumberAttributeName)),
    UPGRADE_FIRMWARE_URL(RequiredPropertySpecFactory.newInstance().stringPropertySpec(firmwareUpdateURLAttributeName)),
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(firmwareUpdateURLAttributeName),
            RequiredPropertySpecFactory.newInstance().datePropertySpec(firmwareUpdateActivationDateAttributeName)),;

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
