package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimplv2.messages.nls.Thesaurus;
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
public enum DisplayDeviceMessage implements DeviceMessageSpec {

    CONSUMER_MESSAGE_CODE_TO_PORT_P1(0, "Send a code message to the P1 port") {
        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.p1InformationAttributeName, DeviceMessageConstants.p1InformationAttributeDefaultTranslation));
        }
    },
    CONSUMER_MESSAGE_TEXT_TO_PORT_P1(1, "Send a text message to the P1 port") {
        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.p1InformationAttributeName, DeviceMessageConstants.p1InformationAttributeDefaultTranslation));
        }
    },
    SET_DISPLAY_MESSAGE(2, "Set display message") {
        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.DisplayMessageAttributeName, DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation));
        }
    },
    SET_DISPLAY_MESSAGE_WITH_OPTIONS(3, "Set display message with options") {
        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(DeviceMessageConstants.DisplayMessageAttributeName, DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation),
                    this.bigDecimalSpec(DeviceMessageConstants.DisplayMessageTimeDurationAttributeName, DeviceMessageConstants.DisplayMessageTimeDurationAttributeDefaultTranslation),
                    this.dateTimeSpec(DeviceMessageConstants.DisplayMessageActivationDate, DeviceMessageConstants.DisplayMessageActivationDefaultTranslation)
            );
        }
    },
    SET_DISPLAY_MESSAGE_ON_IHD_WITH_OPTIONS(4, "Set display message on IHD with options") {
        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(DeviceMessageConstants.DisplayMessageAttributeName, DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation),
                    this.bigDecimalSpec(DeviceMessageConstants.DisplayMessageTimeDurationAttributeName, DeviceMessageConstants.DisplayMessageTimeDurationAttributeDefaultTranslation),
                    this.dateTimeSpec(DeviceMessageConstants.DisplayMessageActivationDate, DeviceMessageConstants.DisplayMessageActivationDefaultTranslation)
            );
        }
    },
    CLEAR_DISPLAY_MESSAGE(5, "Clear display message") {
        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    DisplayDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected PropertySpec stringSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec bigDecimalSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec dateTimeSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.DISPLAY;
    }

    @Override
    public String getName() {
        return Services
                .nlsService()
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    private String getNameResourceKey() {
        return DisplayDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public long getMessageId() {
        return id;
    }

}