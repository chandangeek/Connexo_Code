package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ZigBeeConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    CreateHANNetwork(DeviceMessageId.ZIGBEE_CONFIGURATION_CREATE_HAN_NETWORK, "Create HAN network"),
    RemoveHANNetwork(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_HAN_NETWORK, "Remove HAN network"),
    JoinZigBeeSlaveDevice(DeviceMessageId.ZIGBEE_CONFIGURATION_JOIN_SLAVE_DEVICE, "Join ZigBee slave device") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            HexStringFactory factory = new HexStringFactory();
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationZigBeeAddressAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationZigBeeLinkKeyAttributeName, true, factory));
        }
    },
    RemoveMirror(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_MIRROR, "Remove mirror") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationMirrorAddressAttributeName, true, new HexStringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationForceRemovalAttributeName, true, new BooleanFactory()));
        }
    },
    RemoveZigBeeSlaveDevice(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_SLAVE_DEVICE, "Remove single ZigBee slave device") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationZigBeeAddressAttributeName, true, new HexStringFactory()));
        }
    },
    RemoveAllZigBeeSlaveDevices(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_ALL_SLAVE_DEVICES, "Remove all ZigBee slave devices"),
    BackUpZigBeeHANParameters(DeviceMessageId.ZIGBEE_CONFIGURATION_BACK_UP_HAN_PARAMETERS, "Backup ZigBee HAN parameters"),
    RestoreZigBeeHANParameters(DeviceMessageId.ZIGBEE_CONFIGURATION_RESTORE_HAN_PARAMETERS, "Restore ZigBee HAN parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(ZigBeeConfigurationHANRestoreUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    ReadZigBeeStatus(DeviceMessageId.ZIGBEE_CONFIGURATION_READ_STATUS, "Read ZigBee status"),
    ChangeZigBeeHANStartupAttributeSetup(DeviceMessageId.ZIGBEE_CONFIGURATION_CHANGE_HAN_STARTUP_ATTRIBUTE_SETUP, "Change ZigBee HAN startup attribute setup") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationSASExtendedPanIdAttributeName, true, new HexStringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationSASPanIdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationSASPanChannelMaskAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationSASInsecureJoinAttributeName, true, new BooleanFactory()));
        }
    },
    ZigBeeNCPFirmwareUpdateWithUserFile(DeviceMessageId.ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE, "ZigBee NCP firmware update with user file") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(ZigBeeConfigurationFirmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    ZigBeeNCPFirmwareUpdateWithUserFileAndActivate(DeviceMessageId.ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE_AND_ACTIVATE, "ZigBee NCP firmware update with user file and activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(ZigBeeConfigurationFirmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(ZigBeeConfigurationActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    ZigBeeConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return ZigBeeConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}