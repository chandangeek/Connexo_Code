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

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ZigBeeConfigurationDeviceMessage implements DeviceMessageSpec {

    CreateHANNetwork,
    RemoveHANNetwork,
    JoinZigBeeSlaveDevice(
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName)
    ),
    RemoveMirror(
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName),
            RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName)
    ),
    RemoveZigBeeSlaveDevice(RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName)),
    RemoveAllZigBeeSlaveDevices,
    BackUpZigBeeHANParameters,
    RestoreZigBeeHANParameters(
            RequiredPropertySpecFactory.newInstance().
                    referencePropertySpec(
                            DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName,
                            (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id()))),
    ReadZigBeeStatus,
    ChangeZigBeeHANStartupAttributeSetup(
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeName),
            RequiredPropertySpecFactory.newInstance().booleanPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeName)
    ),
    ZigBeeNCPFirmwareUpdateWithUserFile(
            RequiredPropertySpecFactory.newInstance().
                    referencePropertySpec(
                            DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName,
                            (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id()))),
    ZigBeeNCPFirmwareUpdateWithUserFileAndActivate(
            RequiredPropertySpecFactory.newInstance().
                    referencePropertySpec(
                            DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName,
                            (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id())),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName)
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