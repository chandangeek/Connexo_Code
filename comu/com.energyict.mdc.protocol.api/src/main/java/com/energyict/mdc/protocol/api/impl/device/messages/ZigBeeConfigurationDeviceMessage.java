/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

enum ZigBeeConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    CreateHANNetwork(DeviceMessageId.ZIGBEE_CONFIGURATION_CREATE_HAN_NETWORK, "Create HAN network"),
    RemoveHANNetwork(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_HAN_NETWORK, "Remove HAN network"),
    JoinZigBeeSlaveDevice(DeviceMessageId.ZIGBEE_CONFIGURATION_JOIN_SLAVE_DEVICE, "Join ZigBee slave device") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationZigBeeAddressAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationZigBeeLinkKeyAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    RemoveMirror(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_MIRROR, "Remove mirror") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationMirrorAddressAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationForceRemovalAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    RemoveZigBeeSlaveDevice(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_SLAVE_DEVICE, "Remove single ZigBee slave device") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationZigBeeAddressAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    RemoveAllZigBeeSlaveDevices(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_ALL_SLAVE_DEVICES, "Remove all ZigBee slave devices"),
    BackUpZigBeeHANParameters(DeviceMessageId.ZIGBEE_CONFIGURATION_BACK_UP_HAN_PARAMETERS, "Backup ZigBee HAN parameters"),
    RestoreZigBeeHANParameters(DeviceMessageId.ZIGBEE_CONFIGURATION_RESTORE_HAN_PARAMETERS, "Restore ZigBee HAN parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(DeviceMessageFile.class)
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationHANRestoreUserFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ReadZigBeeStatus(DeviceMessageId.ZIGBEE_CONFIGURATION_READ_STATUS, "Read ZigBee status"),
    ChangeZigBeeHANStartupAttributeSetup(DeviceMessageId.ZIGBEE_CONFIGURATION_CHANGE_HAN_STARTUP_ATTRIBUTE_SETUP, "Change ZigBee HAN startup attribute setup") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationSASExtendedPanIdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationSASPanIdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationSASPanChannelMaskAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationSASInsecureJoinAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ZigBeeNCPFirmwareUpdateWithUserFile(DeviceMessageId.ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE, "ZigBee NCP firmware update with file") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(DeviceMessageFile.class)
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ZigBeeNCPFirmwareUpdateWithUserFileAndActivate(DeviceMessageId.ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE_AND_ACTIVATE, "ZigBee NCP firmware update with user file and activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(DeviceMessageFile.class)
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(ZigBeeConfigurationDeviceMessageAttributes.ZigBeeConfigurationActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

}