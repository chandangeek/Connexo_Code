package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ZigBeeConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    CreateHANNetwork(0, "Create HAN network") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    RemoveHANNetwork(1, "Remove HAN network") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    JoinZigBeeSlaveDevice(2, "Join ZigBee slave device") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName, DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeDefaultTranslation),
                    this.passwordSpec(service, DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName, DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeDefaultTranslation)
            );
        }
    },
    RemoveMirror(3, "Remove mirror") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName, DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName, DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeDefaultTranslation)
            );
        }
    },
    RemoveZigBeeSlaveDevice(4, "Remove ZigBee slave device") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.hexStringSpec(service, DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName, DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeDefaultTranslation));
        }
    },
    RemoveAllZigBeeSlaveDevices(5, "Remove all ZigBee slave devices") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    BackUpZigBeeHANParameters(6, "Backup ZigBee HAN parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    RestoreZigBeeHANParameters(7, "Restore ZigBee HAN parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.messageFileSpec(service, DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName, DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeDefaultTranslation));
        }
    },
    ReadZigBeeStatus(8, "Read ZigBee status") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ChangeZigBeeHANStartupAttributeSetup(9, "Change ZigBee HAN startup attribute setup") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeName, DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeName, DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeName, DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeName, DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeDefaultTranslation)
            );
        }
    },
    ZigBeeNCPFirmwareUpdateWithUserFile(10, "ZigBee NCP firmware update with user file") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.messageFileSpec(service, DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName, DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeDefaultTranslation));
        }
    },
    ZigBeeNCPFirmwareUpdateWithUserFileAndActivate(11, "ZigBee NCP firmware update with user file and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.messageFileSpec(service, DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName, DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName, DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeDefaultTranslation)
            );
        }
    },
    UpdateLinkKey(12, "Update HAN link key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.hexStringSpec(service, DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName, DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeDefaultTranslation));
        }
    },
    JoinZigBeeSlaveFromDeviceType(13, "Join ZigBee slave device") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName, DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeDefaultTranslation),
                    this.passwordSpec(service, DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName, DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ZigBeeConfigurationDeviceType, DeviceMessageConstants.ZigBeeConfigurationDeviceTypeDefaultTranslation)
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    ZigBeeConfigurationDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    private PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec passwordSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .passwordSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec messageFileSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceMessageFile.class)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    private String getNameResourceKey() {
        return ZigBeeConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

   @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.ZIGBEE_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}