package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.DisplayMessageActivationDate;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.DisplayMessageAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.DisplayMessageTimeDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.p1InformationAttributeName;

/**
 * Provides a summary of all messages related to a <i>Display</i>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum DisplayDeviceMessage implements DeviceMessageSpecEnum {

    CONSUMER_MESSAGE_CODE_TO_PORT_P1(DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_CODE_TO_PORT_P1, "Send a code message tot he P1 port") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(p1InformationAttributeName, true, new StringFactory()));
        }
    },
    CONSUMER_MESSAGE_TEXT_TO_PORT_P1(DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_TEXT_TO_PORT_P1, "Send a text message to the P1 port") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(p1InformationAttributeName, true, new StringFactory()));
        }
    },
    SET_DISPLAY_MESSAGE(DeviceMessageId.DISPLAY_SET_MESSAGE, "Set display message") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DisplayMessageAttributeName, true, new StringFactory()));
        }
    },
    SET_DISPLAY_MESSAGE_WITH_OPTIONS(DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS, "Set display message with options") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DisplayMessageAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DisplayMessageTimeDurationAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DisplayMessageActivationDate, true, new DateAndTimeFactory()));
        }
    },
    SET_DISPLAY_MESSAGE_ON_IHD_WITH_OPTIONS(DeviceMessageId.DISPLAY_SET_MESSAGE_ON_IHD_WITH_OPTIONS, "Set display message on the in-home display with options") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DisplayMessageAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DisplayMessageTimeDurationAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DisplayMessageActivationDate, true, new DateAndTimeFactory()));
        }
    },
    CLEAR_DISPLAY_MESSAGE(DeviceMessageId.DISPLAY_CLEAR_MESSAGE, "Clear display message");

    private DeviceMessageId id;
    private String defaultTranslation;

    DisplayDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return DisplayDeviceMessage.class.getSimpleName() + "." + this.toString();
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

}