package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides a summary of all <i>Firmware</i> related messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
enum FirmwareDeviceMessage implements DeviceMessageSpecEnum {

    UPGRADE_FIRMWARE_WITH_USER_FILE_ACTIVATE_LATER(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER, "Upload firmware and activate later") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.referenceSpec(BaseFirmwareVersion.class)
                    .named(FirmwareDeviceMessageAttributes.firmwareUpdateFileAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_ACTIVATE_IMMEDIATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE, "Upload firmware and activate immediately") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.referenceSpec(BaseFirmwareVersion.class)
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE, "Upload firmware with resume option and activate immediately") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.referenceSpec(BaseFirmwareVersion.class)
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService.booleanSpec()
                            .named(FirmwareDeviceMessageAttributes.resumeFirmwareUpdateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE, "Upload firmware with resume option and type and activate immediately") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.referenceSpec(BaseFirmwareVersion.class)
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService.booleanSpec()
                            .named(FirmwareDeviceMessageAttributes.resumeFirmwareUpdateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService.booleanSpec()
                            .named(FirmwareDeviceMessageAttributes.plcTypeFirmwareUpdateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_ACTIVATE(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE, "Activate last uploaded firmware") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.specForValuesOf(new DateAndTimeFactory())
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_DATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE, "Upload firmware with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.referenceSpec(BaseFirmwareVersion.class)
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService.specForValuesOf(new DateAndTimeFactory())
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE_DATE(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_VERSION_AND_ACTIVATE_DATE, "Upload firmware with version and activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.referenceSpec(BaseFirmwareVersion.class)
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService.specForValuesOf(new DateAndTimeFactory())
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService.stringSpec()
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateVersionNumberAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_URL_ACTIVATE_IMMEDIATE(DeviceMessageId.FIRMWARE_UPGRADE_URL_ACTIVATE_IMMEDIATE, "Upload firmware via url and activate immediately") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.stringSpec()
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateURLAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE_DATE(DeviceMessageId.FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE, "Upload firmware via url with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.stringSpec()
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateURLAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService.specForValuesOf(new DateAndTimeFactory())
                            .named(FirmwareDeviceMessageAttributes.firmwareUpdateActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    }

    public abstract Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption();

}