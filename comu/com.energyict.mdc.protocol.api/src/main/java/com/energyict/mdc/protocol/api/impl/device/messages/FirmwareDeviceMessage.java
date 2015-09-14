package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateURLAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
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

    UPGRADE_FIRMWARE_WITH_USER_FILE_ACTIVATE_LATER(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER, "Upload firmware and activate later") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateFileAttributeName, true, FactoryIds.FIRMWAREVERSION));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_ACTIVATE_IMMEDIATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE, "Upload firmware and activate immediately") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateFileAttributeName, true, FactoryIds.FIRMWAREVERSION));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE, "Upload firmware with resume option and activate immediately") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateFileAttributeName, true, FactoryIds.FIRMWAREVERSION));
            propertySpecs.add(propertySpecService.basicPropertySpec(resumeFirmwareUpdateAttributeName, true, new BooleanFactory()));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE, "Upload firmware with resume option and type and activate immediately") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateFileAttributeName, true, FactoryIds.FIRMWAREVERSION));
            propertySpecs.add(propertySpecService.basicPropertySpec(resumeFirmwareUpdateAttributeName, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(plcTypeFirmwareUpdateAttributeName, true, new BooleanFactory()));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_ACTIVATE(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE, "Activate last uploaded firmware") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateAndTimeFactory()));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_DATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE, "Upload firmware with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateFileAttributeName, true, FactoryIds.FIRMWAREVERSION));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateAndTimeFactory()));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE_DATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_VERSION_AND_ACTIVATE_DATE, "Upload firmware with version and activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateFileAttributeName, true, FactoryIds.FIRMWAREVERSION));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateVersionNumberAttributeName, true, new StringFactory()));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_URL_ACTIVATE_IMMEDIATE(DeviceMessageId.FIRMWARE_UPGRADE_URL_ACTIVATE_IMMEDIATE, "Upload firmware via url and activate immediately") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateURLAttributeName, true, new StringFactory()));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE_DATE(DeviceMessageId.FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE, "Upload firmware via url with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateURLAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateAndTimeFactory()));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    FirmwareDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return FirmwareDeviceMessage.class.getSimpleName() + "." + this.toString();
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

    public abstract Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption();
}