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

public enum LoggingConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SET_SERVER_LOG_LEVEL(DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_SERVER_LOG_LEVEL, "Set server log level") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .boundedBigDecimalSpec(BigDecimal.ZERO, BigDecimal.valueOf(7))
                            .named(DeviceMessageAttributes.logLevel)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },

    SET_WEB_PORTAL_LOG_LEVEL(DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_WEB_PORTAL_LOG_LEVEL, "Set web portal log level") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .boundedBigDecimalSpec(BigDecimal.ZERO, BigDecimal.valueOf(7))
                            .named(DeviceMessageAttributes.logLevel)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    DOWNLOAD_FILE(DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_DOWNLOAD_FILE, "Download file") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                propertySpecService
                    .stringSpec()
                    .named(DeviceMessageAttributes.fileInfo)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                        .finish());
        }
    },
    PUSH_CONFIGURATION(DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_PUSH_CONFIGURATION, "Push the configuration"),
    PUSH_LOGS_NOW(DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_PUSH_LOGS_NOW, "Push the logs now")
    ,

    ;

    private DeviceMessageId id;
    private String defaultTranslation;

    LoggingConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return ClockDeviceMessage.class.getSimpleName() + "." + this.toString();
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
    }

}
