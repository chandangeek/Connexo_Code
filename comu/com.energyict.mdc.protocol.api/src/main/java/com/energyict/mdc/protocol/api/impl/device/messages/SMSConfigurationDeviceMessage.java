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
public enum SMSConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetSmsDataNbr(DeviceMessageId.SMS_CONFIGURATION_SET_DATA_NUMBER, "Set SMS data number") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSmsDataNbrAttributeName;
        }
    },
    SetSmsAlarmNbr(DeviceMessageId.SMS_CONFIGURATION_SET_ALARM_NUMBER, "Set SMS alarm number") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSmsAlarmNbrAttributeName;
        }
    },
    SetSmsEvery(DeviceMessageId.SMS_CONFIGURATION_SET_EVERY, "Set SMS every") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSmsEveryAttributeName;
        }
    },
    SetSmsNbr(DeviceMessageId.SMS_CONFIGURATION_SET_NUMBER, "Set SMS number") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSmsNbrAttributeName;
        }
    },
    SetSmsCorrection(DeviceMessageId.SMS_CONFIGURATION_SET_CORRECTION, "Set SMS correction") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSmsCorrectionAttributeName;
        }
    },
    SetSmsConfig(DeviceMessageId.SMS_CONFIGURATION_SET_CONFIG, "Set SMS configuration") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetSmsConfigAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    SMSConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return SMSConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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