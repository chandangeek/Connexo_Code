package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/9/14
 * Time: 3:29 PM
 */
public enum LoggingConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SET_SERVER_LOG_LEVEL(DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_SERVER_LOG_LEVEL, "Set server log level") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.boundedDecimalPropertySpec(DeviceMessageConstants.logLevel, true, BigDecimal.valueOf(0), BigDecimal.valueOf(7)));
        }
    },

    SET_WEB_PORTAL_LOG_LEVEL(DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_WEB_PORTAL_LOG_LEVEL, "Set web portal log level") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.boundedDecimalPropertySpec(DeviceMessageConstants.logLevel, true, BigDecimal.valueOf(0), BigDecimal.valueOf(7)));
        }
    },
    DOWNLOAD_FILE(DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_DOWNLOAD_FILE, "Download file") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.fileInfo, true, ""));
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    }

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}
