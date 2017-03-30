/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

enum AlarmConfigurationMessage implements DeviceMessageSpecEnum {

    RESET_ALL_ALARM_BITS(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS, "Reset all alarm bits"),
    WRITE_ALARM_FILTER(DeviceMessageId.ALARM_CONFIGURATION_WRITE_ALARM_FILTER, "Write alarm filter") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.alarmFilterAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    }, CONFIGURE_PUSH_EVENT_NOTIFICATION(DeviceMessageId.ALARM_CONFIGURATION_CONFIGURE_PUSH_EVENT_NOTIFICATION, "Configure push event notification"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.transportTypeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.destinationAddressAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.messageTypeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    RESET_ALL_ERROR_BITS(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS, "Reset all error bits");

    private DeviceMessageId id;
    private String defaultTranslation;

    AlarmConfigurationMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return AlarmConfigurationMessage.class.getSimpleName() + "." + this.toString();
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