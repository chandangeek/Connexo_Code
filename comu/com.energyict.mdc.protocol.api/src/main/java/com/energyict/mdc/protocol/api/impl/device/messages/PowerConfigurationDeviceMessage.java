package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PowerConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    IEC1107LimitPowerQuality(DeviceMessageId.POWER_CONFIGURATION_IEC1107_LIMIT_POWER_QUALITY, "Limit power quality") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.powerQualityThresholdAttributeName;
        }
    },
    SetReferenceVoltage(DeviceMessageId.POWER_CONFIGURATION_SET_REFERENCE_VOLTAGE, "Set reference voltage") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.ReferenceVoltageAttributeName;
        }
    },
    SetVoltageSagTimeThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SAG_TIME_THRESHOLD, "Set voltage sag time treshold") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.VoltageSagTimeThresholdAttributeName;
        }
    },
    SetVoltageSwellTimeThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SWELL_TIME_THRESHOLD, "Set voltage swell time treshold") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.VoltageSwellTimeThresholdAttributeName;
        }
    },
    SetVoltageSagThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SAG_THRESHOLD, "Set voltage sag threshold") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.VoltageSagThresholdAttributeName;
        }
    },
    SetVoltageSwellThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_VOLTAGE_SWELL_THRESHOLD, "Set voltage swell threshold") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.VoltageSwellThresholdAttributeName;
        }
    },
    SetLongPowerFailureTimeThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_LONG_POWER_FAILURE_TIME_THRESHOLD, "Set long power failure time treshold") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.LongPowerFailureTimeThresholdAttributeName;
        }
    },
    SetLongPowerFailureThreshold(DeviceMessageId.POWER_CONFIGURATION_SET_LONG_POWER_FAILURE_THRESHOLD, "Set long power failure treshold") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.LongPowerFailureThresholdAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    PowerConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return PowerConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(this.bigDecimalProperty(this.propertyName(), propertySpecService));
        return propertySpecs;
    }

    private PropertySpec bigDecimalProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new BigDecimalFactory());
    }

    protected abstract String propertyName();

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}