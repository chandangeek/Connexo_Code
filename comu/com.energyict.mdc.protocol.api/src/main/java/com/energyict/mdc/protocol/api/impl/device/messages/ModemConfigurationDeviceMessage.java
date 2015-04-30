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
public enum ModemConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetDialCommand(DeviceMessageId.MODEM_CONFIGURATION_SET_DIAL_COMMAND, "Set dial command") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetDialCommandAttributeName;
        }
    },
    SetModemInit1(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEM_INIT_1, "Set modem init1") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetModemInit1AttributeName;
        }
    },
    SetModemInit2(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEM_INIT_2, "Set modem init2") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetModemInit2AttributeName;
        }
    },
    SetModemInit3(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEM_INIT_3, "Set modem init3") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetModemInit3AttributeName;
        }
    },
    SetPPPBaudRate(DeviceMessageId.MODEM_CONFIGURATION_SET_PPP_BAUD_RATE, "Set PPP baud rate") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetPPPBaudRateAttributeName;
        }
    },
    SetModemtype(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEMTYPE, "Set modem type") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetModemtypeAttributeName;
        }
    },
    SetResetCycle(DeviceMessageId.MODEM_CONFIGURATION_SET_RESET_CYCLE, "Set reset cycle") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetResetCycleAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    ModemConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return ModemConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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