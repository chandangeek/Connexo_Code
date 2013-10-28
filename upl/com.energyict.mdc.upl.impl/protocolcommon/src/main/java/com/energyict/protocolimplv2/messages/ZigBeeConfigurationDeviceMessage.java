package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ZigBeeConfigurationDeviceMessage implements DeviceMessageSpec {

    CreateHANNetwork,
    RemoveHANNetwork,
    JoinZigBeeSlaveDevice(
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.zigBeeConfigurationZigBeeAddressAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.zigBeeConfigurationZigBeeLinkKeyAttributeName)
    ),
    RemoveMirror(
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.zigBeeConfigurationMirrorAddressAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.zigBeeConfigurationForceRemovalAttributeName)
    ),
    RemoveZigBeeSlaveDevice(PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.zigBeeConfigurationZigBeeAddressAttributeName)),
    RemoveAllZigBeeSlaveDevices,
    BackUpZigBeeHANParameters,
    RestoreZigBeeHANParameters(PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.zigBeeConfigurationHANRestoreUserFileAttributeName)),
    ReadZigBeeStatus,
    ChangeZigBeeHANStartupAttributeSetup(
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.zigBeeConfigurationSASExtendedPanIdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.zigBeeConfigurationSASPanIdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.zigBeeConfigurationSASPanChannelMaskAttributeName),
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.zigBeeConfigurationSASInsecureJoinAttributeName)
    ),
    ZigBeeNCPFirmwareUpdateWithUserFile(PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.zigBeeConfigurationFirmwareUpdateUserFileAttributeName)),
    ZigBeeNCPFirmwareUpdateWithUserFileAndActivate(
            PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.zigBeeConfigurationFirmwareUpdateUserFileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.zigBeeConfigurationActivationDateAttributeName)
    );

    private static final DeviceMessageCategory category = DeviceMessageCategories.ZIGBEE_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private ZigBeeConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
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
        return ZigBeeConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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