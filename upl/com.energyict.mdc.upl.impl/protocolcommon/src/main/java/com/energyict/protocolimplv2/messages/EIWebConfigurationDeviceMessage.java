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
public enum EIWebConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    SetEIWebPassword(0, "Set EIWeb password") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebPasswordAttributeName, DeviceMessageConstants.SetEIWebPasswordAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebPage(1, "Set EIWeb web page") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebPageAttributeName, DeviceMessageConstants.SetEIWebPageAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebFallbackPage(2, "Set EIWeb fallback page") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebFallbackPageAttributeName, DeviceMessageConstants.SetEIWebFallbackPageAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebSendEvery(3, "Set EIWeb send every") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebSendEveryAttributeName, DeviceMessageConstants.SetEIWebSendEveryAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebCurrentInterval(4, "Set EIWeb current interval") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebCurrentIntervalAttributeName, DeviceMessageConstants.SetEIWebCurrentIntervalAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebDatabaseID(5, "Set EIWeb database ID") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebDatabaseIDAttributeName, DeviceMessageConstants.SetEIWebDatabaseIDAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebOptions(6, "Set EIWeb web options") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebOptionsAttributeName, DeviceMessageConstants.SetEIWebOptionsAttributeDefaultTranslation)
            );
        }
    },
    UpdateEIWebSSLCertificate(7, "Update the SSL certificate for EIWeb+") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.sslCertificateUserFile, DeviceMessageConstants.sslCertificateUserFileDefaultTranslation));
        }
    },
    EIWebSetOption(8, "EIWeb - Set an option") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation));
        }
    },
    EIWebClrOption(21, "EIWeb - Clear an option") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation));
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    EIWebConfigurationDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec deviceMessageFileSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceMessageFile.class)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... exhaustiveValues) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(exhaustiveValues)
                .markExhaustive()
                .finish();
    }

    private String getNameResourceKey() {
        return EIWebConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.EIWEB_PARAMETERS,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}