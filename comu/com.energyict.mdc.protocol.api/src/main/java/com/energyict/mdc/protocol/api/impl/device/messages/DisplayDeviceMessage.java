/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

enum DisplayDeviceMessage implements DeviceMessageSpecEnum {

    CONSUMER_MESSAGE_CODE_TO_PORT_P1(DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_CODE_TO_PORT_P1, "Send a code message to the P1 port") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DisplayDeviceMessageAttributes.p1InformationAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CONSUMER_MESSAGE_TEXT_TO_PORT_P1(DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_TEXT_TO_PORT_P1, "Send a text message to the P1 port") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DisplayDeviceMessageAttributes.p1InformationAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_DISPLAY_MESSAGE(DeviceMessageId.DISPLAY_SET_MESSAGE, "Set display message") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DisplayDeviceMessageAttributes.DisplayMessageAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_DISPLAY_MESSAGE_WITH_OPTIONS(DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS, "Set display message with options") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DisplayDeviceMessageAttributes.DisplayMessageAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DisplayDeviceMessageAttributes.DisplayMessageTimeDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DisplayDeviceMessageAttributes.DisplayMessageActivationDate)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_DISPLAY_MESSAGE_ON_IHD_WITH_OPTIONS(DeviceMessageId.DISPLAY_SET_MESSAGE_ON_IHD_WITH_OPTIONS, "Set display message on the in-home display with options") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DisplayDeviceMessageAttributes.DisplayMessageAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DisplayDeviceMessageAttributes.DisplayMessageTimeDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DisplayDeviceMessageAttributes.DisplayMessageActivationDate)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

}