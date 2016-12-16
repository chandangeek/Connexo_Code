package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides a summary of all messages related to pricing.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum PricingInformationMessage implements DeviceMessageSpecSupplier {

    ReadPricingInformation(9001, "Read pricing information") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SetPricingInformation(9002, "Set pricing information") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.deviceMessageFileSpec(service, DeviceMessageConstants.PricingInformationUserFileAttributeName, DeviceMessageConstants.PricingInformationUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.PricingInformationActivationDateAttributeName, DeviceMessageConstants.PricingInformationActivationDateAttributeDefaultTranslation)
            );
        }
    },
    SetStandingChargeAndActivationDate(9005, "Set standing charge and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.StandingChargeAttributeName, DeviceMessageConstants.StandingChargeAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.PricingInformationActivationDateAttributeName, DeviceMessageConstants.PricingInformationActivationDateAttributeDefaultTranslation)
            );
        }
    },
    SetStandingCharge(9003, "Set standing charge") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.StandingChargeAttributeName, DeviceMessageConstants.StandingChargeAttributeDefaultTranslation));
        }
    },
    UpdatePricingInformation(9004, "Update pricing information") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.PricingInformationUserFileAttributeName, DeviceMessageConstants.PricingInformationUserFileAttributeDefaultTranslation));
        }
    },
    SEND_NEW_TARIFF(9006, "Send new TOU tariff") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.contractsXmlUserFileAttributeName, DeviceMessageConstants.contractsXmlUserFileAttributeDefaultTranslation));
        }
    },
    SEND_NEW_PRICE_MATRIX(9007, "Send new price matrix") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.contractsXmlUserFileAttributeName, DeviceMessageConstants.contractsXmlUserFileAttributeDefaultTranslation));
        }
    },
    SET_CURRENCY_AND_ACTIVATION_DATE(9008, "Set currency and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.currency, DeviceMessageConstants.currencyDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.PricingInformationActivationDateAttributeName, DeviceMessageConstants.PricingInformationActivationDateAttributeDefaultTranslation)
            );
        }
    },
    SET_CURRENCY(9009, "Set currency") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.currency, DeviceMessageConstants.currencyDefaultTranslation));
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    PricingInformationMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
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

    protected PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec deviceMessageFileSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceMessageFile.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return PricingInformationMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.LOAD_BALANCE,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}