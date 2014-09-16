package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateURLAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateVersionNumberAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;

/**
 * Provides a summary of all <i>Firmware</i> related messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum FirmwareDeviceMessage implements DeviceMessageSpecEnum {

    UPGRADE_FIRMWARE_WITH_USER_FILE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE, "Firmware upgrade via user file") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION, "Firmware upgrade via user file with resume option") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(resumeFirmwareUpdateAttributeName, true, new BooleanFactory()));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE, "Firmware upgrade via user file with resumt option and type") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(resumeFirmwareUpdateAttributeName, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(plcTypeFirmwareUpdateAttributeName, true, new BooleanFactory()));
        }
    },
    UPGRADE_FIRMWARE_ACTIVATE(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE, "Active last uploaded firmware") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateFactory()));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE, "Firmware upgrade via user file with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateFactory()));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_VERSION_AND_ACTIVATE, "Firmware upgrade via user file with version and activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateVersionNumberAttributeName, true, new StringFactory()));
        }
    },
    UPGRADE_FIRMWARE_URL(DeviceMessageId.FIRMWARE_UPGRADE_URL, "Firmware upgrade via url") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateURLAttributeName, true, new StringFactory()));
        }
    },
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE(DeviceMessageId.FIRMWARE_UPGRADE_URL_AND_ACTIVATE, "Firmware upgrade via url with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateURLAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateFactory()));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    FirmwareDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getNameResourceKey() {
        return FirmwareDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String defaultTranslation() {
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