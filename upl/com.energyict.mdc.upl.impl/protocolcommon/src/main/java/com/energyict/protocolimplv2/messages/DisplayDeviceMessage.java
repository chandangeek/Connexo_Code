package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides a summary of all messages related to a <i>Display</i>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum DisplayDeviceMessage implements DeviceMessageSpecSupplier {

    CONSUMER_MESSAGE_CODE_TO_PORT_P1(10001, "Send a code message to the P1 port") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.p1InformationAttributeName, DeviceMessageConstants.p1InformationAttributeDefaultTranslation));
        }
    },
    CONSUMER_MESSAGE_TEXT_TO_PORT_P1(10002, "Send a text message to the P1 port") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.p1InformationAttributeName, DeviceMessageConstants.p1InformationAttributeDefaultTranslation));
        }
    },
    SET_DISPLAY_MESSAGE(10003, "Set display message") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.DisplayMessageAttributeName, DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation));
        }
    },
    SET_DISPLAY_MESSAGE_WITH_OPTIONS(10004, "Set display message with options") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.DisplayMessageAttributeName, DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.DisplayMessageTimeDurationAttributeName, DeviceMessageConstants.DisplayMessageTimeDurationAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.DisplayMessageActivationDate, DeviceMessageConstants.DisplayMessageActivationDefaultTranslation)
            );
        }
    },
    SET_DISPLAY_MESSAGE_ON_IHD_WITH_OPTIONS(10005, "Set display message on IHD with options") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.DisplayMessageAttributeName, DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.DisplayMessageTimeDurationAttributeName, DeviceMessageConstants.DisplayMessageTimeDurationAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.DisplayMessageActivationDate, DeviceMessageConstants.DisplayMessageActivationDefaultTranslation)
            );
        }
    },
    CLEAR_DISPLAY_MESSAGE(10006, "Clear display message") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    DisplayDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
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

    private String getNameResourceKey() {
        return DisplayDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.DISPLAY,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}