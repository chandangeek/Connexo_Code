package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HexString;
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
public enum MBusSetupDeviceMessage implements DeviceMessageSpecSupplier {

    Decommission(24001, "Decommission") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    DataReadout(24002, "Data readout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    Commission(24003, "Commission") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    DecommissionAll(24004, "Decommission all") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    SetEncryptionKeys(24005, "Set encryption key") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.passwordSpec(service, DeviceMessageConstants.openKeyAttributeName, DeviceMessageConstants.openKeyAttributeDefaultTranslation),
                    this.passwordSpec(service, DeviceMessageConstants.transferKeyAttributeName, DeviceMessageConstants.transferKeyAttributeDefaultTranslation)
            );
        }
    },
    SetEncryptionKeysUsingCryptoserver(24006, "Set encryption key using cryptoserver") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.hexStringSpec(service, DeviceMessageConstants.defaultKeyAttributeName, DeviceMessageConstants.defaultKeyAttributeDefaultTranslation));
        }
    },
    UseCorrectedValues(24007, "Use corrected values") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    UseUncorrectedValues(24008, "Use uncorrected values") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    WriteCaptureDefinition(24012, "Write MBus capture definition") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.dib, DeviceMessageConstants.dibDefaultTranslation),
                    this.hexStringSpec(service, DeviceMessageConstants.vib, DeviceMessageConstants.vibDefaultTranslation)
            );
        }
    },
    Commission_With_Channel(24009, "Install MBus device") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.mbusChannel, DeviceMessageConstants.mbusChannelDefaultTranslation)
                            .setDefaultValue(BigDecimal.ONE)
                            .addValues(
                                    BigDecimal.ZERO,
                                    BigDecimal.ONE,
                                    BigDecimal.valueOf(2),
                                    BigDecimal.valueOf(3),
                                    BigDecimal.valueOf(4))
                            .markExhaustive()
                            .finish()
            );
        }
    },
    Reset_MBus_Client(24013, "Reset MBus client") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.mbusSerialNumber, DeviceMessageConstants.mbusSerialNumberDefaultTranslation));
        }
    },
    WriteCaptureDefinitionForAllInstances(24010, "Write MBus capture definitions") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.dibInstance1, DeviceMessageConstants.dibInstance1DefaultTranslation, getCaptureDefinitionDefaultValue(converter)),
                    this.hexStringSpec(service, DeviceMessageConstants.vibInstance1, DeviceMessageConstants.vibInstance1DefaultTranslation, getCaptureDefinitionDefaultValue(converter)),
                    this.hexStringSpec(service, DeviceMessageConstants.dibInstance2, DeviceMessageConstants.dibInstance2DefaultTranslation, getCaptureDefinitionDefaultValue(converter)),
                    this.hexStringSpec(service, DeviceMessageConstants.vibInstance2, DeviceMessageConstants.vibInstance2DefaultTranslation, getCaptureDefinitionDefaultValue(converter)),
                    this.hexStringSpec(service, DeviceMessageConstants.dibInstance3, DeviceMessageConstants.dibInstance3DefaultTranslation, getCaptureDefinitionDefaultValue(converter)),
                    this.hexStringSpec(service, DeviceMessageConstants.vibInstance3, DeviceMessageConstants.vibInstance3DefaultTranslation, getCaptureDefinitionDefaultValue(converter)),
                    this.hexStringSpec(service, DeviceMessageConstants.dibInstance4, DeviceMessageConstants.dibInstance4DefaultTranslation, getCaptureDefinitionDefaultValue(converter)),
                    this.hexStringSpec(service, DeviceMessageConstants.vibInstance4, DeviceMessageConstants.vibInstance4DefaultTranslation, getCaptureDefinitionDefaultValue(converter))
            );
        }
    },
    WriteMBusCapturePeriod(24011, "Write MBus capture period") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.durationSpec(service, DeviceMessageConstants.capturePeriodAttributeName, DeviceMessageConstants.capturePeriodAttributeDefaultTranslation));
        }
    },
    ChangeMBusAttributes(24014, "Change MBus attributes") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.mbusChannel, DeviceMessageConstants.mbusChannelDefaultTranslation)
                            .setDefaultValue(BigDecimal.ONE)
                            .addValues(
                                    BigDecimal.ZERO,
                                    BigDecimal.ONE,
                                    BigDecimal.valueOf(2),
                                    BigDecimal.valueOf(3),
                                    BigDecimal.valueOf(4))
                            .markExhaustive()
                            .finish(),
                    this.stringSpec(service, DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientIdentificationNumber, DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientIdentificationNumberDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientManufacturerId, DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientManufacturerIdDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientVersion, DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientVersionDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientDeviceType, DeviceMessageConstants.MBusSetupDeviceMessage_ChangeMBusClientDeviceTypeDefaultTranslation));
        }
    },
    MBusClientRemoteCommission(24015, "MBus client remote commission") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.mbusChannel, DeviceMessageConstants.mbusChannelDefaultTranslation)
                            .setDefaultValue(BigDecimal.ONE)
                            .addValues(
                                    BigDecimal.ZERO,
                                    BigDecimal.ONE,
                                    BigDecimal.valueOf(2),
                                    BigDecimal.valueOf(3),
                                    BigDecimal.valueOf(4))
                            .markExhaustive()
                            .finish(),
                    this.stringSpec(service, DeviceMessageConstants.MBusSetupDeviceMessage_mBusClientShortId, DeviceMessageConstants.MBusSetupDeviceMessage_mBusClientShortIdDefaultTranslation)
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    MBusSetupDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    protected HexString getCaptureDefinitionDefaultValue(Converter converter) {
        return converter.hexFromString("FFFFFFFFFFFFFFFFFFFFFF");
    }

    protected PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    private PropertySpecBuilder<HexString> hexStringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .hexStringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.hexStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, HexString defaultValue) {
        return this.hexStringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).setDefaultValue(defaultValue).finish();
    }

    protected PropertySpec passwordSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .passwordSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec durationSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return MBusSetupDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.MBUS_SETUP,
                this.getPropertySpecs(propertySpecService, converter),
                propertySpecService, nlsService, converter);
    }

}