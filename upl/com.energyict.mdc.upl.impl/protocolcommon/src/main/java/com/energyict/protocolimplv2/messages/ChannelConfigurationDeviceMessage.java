package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ChannelConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    SetFunction(29001, "Set function") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.idPropertySpec(service),
                    this.stringSpec(service, DeviceMessageConstants.SetFunctionAttributeName, DeviceMessageConstants.SetFunctionAttributeDefaultTranslation)
            );
        }
    },
    SetParameters(29002, "Set parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.idPropertySpec(service),
                    this.stringSpec(service, DeviceMessageConstants.SetParametersAttributeName, DeviceMessageConstants.SetParametersAttributeDefaultTranslation)
            );
        }
    },
    SetName(29003, "Set name") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.idPropertySpec(service),
                    this.stringSpec(service, DeviceMessageConstants.SetNameAttributeName, DeviceMessageConstants.SetNameAttributeDefaultTranslation)
            );
        }
    },
    SetUnit(29004, "Set unit") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.idPropertySpec(service),
                    this.stringSpec(service, DeviceMessageConstants.SetUnitAttributeName, DeviceMessageConstants.SetUnitAttributeDefaultTranslation)
            );
        }
    },
    SetLPDivisor(29005, "Set LP divisor") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChannelConfigurationChnNbrAttributeName, DeviceMessageConstants.ChannelConfigurationChnNbrAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.DivisorAttributeName, DeviceMessageConstants.DivisorAttributeDefaultTranslation)
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    ChannelConfigurationDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    private PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec idPropertySpec(PropertySpecService service) {
        return this.bigDecimalSpecBuilder(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation)
                .addValues(this.getBigDecimalValues())
                .markExhaustive()
                .finish();
    }

    /**
     * Return range 1 - 32
     */
    private BigDecimal[] getBigDecimalValues() {
        return IntStream.range(1, 32).mapToObj(BigDecimal::valueOf).toArray(BigDecimal[]::new);
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return ChannelConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.CHANNEL_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}