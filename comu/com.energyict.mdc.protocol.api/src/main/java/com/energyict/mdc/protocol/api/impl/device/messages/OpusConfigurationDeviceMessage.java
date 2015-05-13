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
public enum OpusConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetOpusOSNbr(DeviceMessageId.OPUS_CONFIGURATION_SET_OS_NUMBER, "Set opus OS number") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetOpusOSNbrAttributeName;
        }
    },
    SetOpusPassword(DeviceMessageId.OPUS_CONFIGURATION_SET_PASSWORD, "Set opus password") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetOpusPasswordAttributeName;
        }
    },
    SetOpusTimeout(DeviceMessageId.OPUS_CONFIGURATION_SET_TIMEOUT, "Set opus timeout") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetOpusTimeoutAttributeName;
        }
    },
    SetOpusConfig(DeviceMessageId.OPUS_CONFIGURATION_SET_CONFIG, "Set opus configuration") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetOpusConfigAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    OpusConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return OpusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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