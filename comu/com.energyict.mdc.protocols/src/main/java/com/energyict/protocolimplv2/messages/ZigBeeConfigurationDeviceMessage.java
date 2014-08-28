package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ZigBeeConfigurationDeviceMessage implements DeviceMessageSpec {

    CreateHANNetwork,
    RemoveHANNetwork,
    JoinZigBeeSlaveDevice {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName, true, new HexStringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName, true, new HexStringFactory()));
        }
    },
    RemoveMirror {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName, true, new HexStringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName, true, new BooleanFactory()));
        }
    },
    RemoveZigBeeSlaveDevice {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName, true, new HexStringFactory()));
        }
    },
    RemoveAllZigBeeSlaveDevices,
    BackUpZigBeeHANParameters,
    RestoreZigBeeHANParameters {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    ReadZigBeeStatus,
    ChangeZigBeeHANStartupAttributeSetup{
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeName, true, new HexStringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeName, true, new BooleanFactory()));
        }
    },
    ZigBeeNCPFirmwareUpdateWithUserFile {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    ZigBeeNCPFirmwareUpdateWithUserFileAndActivate {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    };

    private static final DeviceMessageCategory category = DeviceMessageCategories.ZIGBEE_CONFIGURATION;

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return this.getNameResourceKey();
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
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, Bus.getPropertySpecService());
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

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