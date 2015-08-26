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

    CreateHANNetwork(0),
    RemoveHANNetwork(1),
    JoinZigBeeSlaveDevice(2,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName)
    ),
    RemoveMirror(3,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName)
    ),
    RemoveZigBeeSlaveDevice(4, PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName)),
    RemoveAllZigBeeSlaveDevices(5),
    BackUpZigBeeHANParameters(6),
    RestoreZigBeeHANParameters(7, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName)),
    ReadZigBeeStatus(8),
    ChangeZigBeeHANStartupAttributeSetup(9,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeName),
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeName)
    ),
    ZigBeeNCPFirmwareUpdateWithUserFile(10, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName)),
    ZigBeeNCPFirmwareUpdateWithUserFileAndActivate(11,
            PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName)
    ),
    UpdateLinkKey(12, PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName)),
    JoinZigBeeSlaveFromDeviceType(13,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ZigBeeConfigurationDeviceType)
    );


    private static final DeviceMessageCategory category = DeviceMessageCategories.ZIGBEE_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private ZigBeeConfigurationDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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

    @Override
    public int getMessageId() {
        return id;
    }
}