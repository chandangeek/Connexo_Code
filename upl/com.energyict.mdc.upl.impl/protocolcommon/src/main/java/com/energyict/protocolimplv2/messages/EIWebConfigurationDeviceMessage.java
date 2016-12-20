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

    SetEIWebPassword(18001, "Set EIWeb password") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebPasswordAttributeName, DeviceMessageConstants.SetEIWebPasswordAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebPage(18002, "Set EIWeb web page") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebPageAttributeName, DeviceMessageConstants.SetEIWebPageAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebFallbackPage(18003, "Set EIWeb fallback page") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebFallbackPageAttributeName, DeviceMessageConstants.SetEIWebFallbackPageAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebSendEvery(18004, "Set EIWeb send every") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebSendEveryAttributeName, DeviceMessageConstants.SetEIWebSendEveryAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebCurrentInterval(18005, "Set EIWeb current interval") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebCurrentIntervalAttributeName, DeviceMessageConstants.SetEIWebCurrentIntervalAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebDatabaseID(18006, "Set EIWeb database ID") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebDatabaseIDAttributeName, DeviceMessageConstants.SetEIWebDatabaseIDAttributeDefaultTranslation)
            );
        }
    },
    SetEIWebOptions(18007, "Set EIWeb web options") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.SetEIWebOptionsAttributeName, DeviceMessageConstants.SetEIWebOptionsAttributeDefaultTranslation)
            );
        }
    },
    UpdateEIWebSSLCertificate(18008, "Update the SSL certificate for EIWeb+") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.sslCertificateUserFile, DeviceMessageConstants.sslCertificateUserFileDefaultTranslation));
        }
    },
    EIWebSetOption(18009, "EIWeb - Set an option") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation));
        }
    },
    EIWebClrOption(18010, "EIWeb - Clear an option") {
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
                .referenceSpec(DeviceMessageFile.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
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

    protected PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
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
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.EIWEB_PARAMETERS,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}