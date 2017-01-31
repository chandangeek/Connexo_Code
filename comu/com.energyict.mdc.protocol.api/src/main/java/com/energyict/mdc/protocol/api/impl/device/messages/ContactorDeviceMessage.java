/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public enum ContactorDeviceMessage implements DeviceMessageSpecEnum {

    CONTACTOR_OPEN(DeviceMessageId.CONTACTOR_OPEN, "Contactor open"),
    CONTACTOR_OPEN_WITH_OUTPUT(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, "Contactor open with output") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(ContactorDeviceMessageAttributes.digitalOutputAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(BigDecimal.ONE, BigDecimals.TWO)
                            .markExhaustive()
                            .finish());
        }
    },
    CONTACTOR_OPEN_WITH_ACTIVATION_DATE(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, "Contactor open with activate date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(ContactorDeviceMessageAttributes.contactorActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CONTACTOR_ARM(DeviceMessageId.CONTACTOR_ARM, "Contactor arm"),
    CONTACTOR_ARM_WITH_ACTIVATION_DATE(DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE, "Contactor arm with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(ContactorDeviceMessageAttributes.contactorActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CONTACTOR_CLOSE(DeviceMessageId.CONTACTOR_CLOSE, "Contactor close"),
    CONTACTOR_CLOSE_WITH_OUTPUT(DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, "Contactor close with output") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(ContactorDeviceMessageAttributes.digitalOutputAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(BigDecimal.ONE, BigDecimals.TWO)
                            .markExhaustive()
                            .finish());
        }
    },
    CONTACTOR_CLOSE_WITH_ACTIVATION_DATE(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, "Contactor close with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(ContactorDeviceMessageAttributes.contactorActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_CONNECT_CONTROL_MODE(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE, "Change the connect control mode") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(ContactorDeviceMessageAttributes.contactorModeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(
                                    BigDecimal.ZERO,
                                    BigDecimal.ONE,
                                    BigDecimals.TWO,
                                    BigDecimals.THREE,
                                    BigDecimals.FOUR,
                                    BigDecimals.FIVE,
                                    BigDecimals.SIX)
                            .markExhaustive()
                            .finish());
        }
    },
    CLOSE_RELAY(DeviceMessageId.CONTACTOR_CLOSE_RELAY, "Close relay") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(ContactorDeviceMessageAttributes.relayNumberAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(BigDecimal.ONE, BigDecimals.TWO)
                            .markExhaustive()
                            .finish());
        }
    },
    OPEN_RELAY(DeviceMessageId.CONTACTOR_OPEN_RELAY, "Open relay") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(ContactorDeviceMessageAttributes.digitalOutputAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(BigDecimal.ONE, BigDecimals.TWO)
                            .markExhaustive()
                            .finish());
        }
    }, SET_RELAY_CONTROL_MODE(DeviceMessageId.CONTACTOR_SET_RELAY_CONTROL_MODE, "Set relay control mode"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(ContactorDeviceMessageAttributes.digitalOutputAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(BigDecimal.ONE, BigDecimals.TWO)
                            .markExhaustive()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(ContactorDeviceMessageAttributes.contactorModeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(
                                    BigDecimal.ZERO,
                                    BigDecimal.ONE,
                                    BigDecimals.TWO,
                                    BigDecimals.THREE,
                                    BigDecimals.FOUR,
                                    BigDecimals.FIVE,
                                    BigDecimals.SIX)
                            .markExhaustive()
                            .finish());
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    ContactorDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    public String getKey() {
        return ContactorDeviceMessage.class.getSimpleName() + "." + this.toString();
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
