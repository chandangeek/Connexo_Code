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
public enum PPPConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetISP1Phone(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_PHONE, "Set ISP1 phone") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetISP1PhoneAttributeName;
        }
    },
    SetISP1Username(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_USERNAME, "Set ISP1 username") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetISP1UsernameAttributeName;
        }
    },
    SetISP1Password(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_PASSWORD, "Set ISP1 password") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetISP1PasswordAttributeName;
        }
    },
    SetISP1Tries(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_TRIES, "Set ISP1 tries") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetISP1TriesAttributeName;
        }
    },
    SetISP2Phone(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_PHONE, "Set ISP2 phone") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetISP2PhoneAttributeName;
        }
    },
    SetISP2Username(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_USERNAME, "Set ISP2 username") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetISP2UsernameAttributeName;
        }
    },
    SetISP2Password(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_PASSWORD, "Set ISP2 password") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetISP2PasswordAttributeName;
        }
    },
    SetISP2Tries(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_TRIES, "Set ISP2 tries") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetISP2TriesAttributeName;
        }
    },
    SetPPPIdleTimeout(DeviceMessageId.PPP_CONFIGURATION_SET_IDLE_TIMEOUT, "Set PPP idle timeout") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPPPIdleTimeoutAttributeName;
        }
    },
    SetPPPRetryInterval(DeviceMessageId.PPP_CONFIGURATION_SET_RETRY_INTERVAL, "Set PPP retry interval") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPPPRetryIntervalAttributeName;
        }
    },
    SetPPPOptions(DeviceMessageId.PPP_CONFIGURATION_SET_OPTIONS, "Set PPP options") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPPPOptionsAttributeName;
        }
    },
    SetPPPIdleTime(DeviceMessageId.PPP_CONFIGURATION_SET_IDLE_TIME, "Set PPP idle time"){
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPPPIdleTime;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    PPPConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return PPPConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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