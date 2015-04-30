package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum DLMSConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetDLMSDeviceID(DeviceMessageId.DLMS_CONFIGURATION_SET_DEVICE_ID, "Set DLMS device ID") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetDLMSDeviceIDAttributeName;
        }
    },
    SetDLMSMeterID(DeviceMessageId.DLMS_CONFIGURATION_SET_METER_ID, "Set DLMS meter ID") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetDLMSMeterIDAttributeName;
        }
    },
    SetDLMSPassword(DeviceMessageId.DLMS_CONFIGURATION_SET_PASSWORD, "Set DLMS password") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetDLMSPasswordAttributeName;
        }
    },
    SetDLMSIdleTime(DeviceMessageId.DLMS_CONFIGURATION_SET_IDLE_TIME, "Set DLMS idle time") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetDLMSIdleTimeAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    DLMSConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }


    @Override
    public String getKey() {
        return DLMSConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(this.stringProperty(this.propertyName(), propertySpecService));
        return propertySpecs;
    }

    private PropertySpec stringProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new StringFactory());
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