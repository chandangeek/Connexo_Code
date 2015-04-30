package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.RadixFormatAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.RegisterAddressAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.RegisterValueAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetMmConfigAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetMmEveryAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetMmInstantAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetMmOverflowAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetMmTimeoutAttributeName;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ModbusConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetMmEvery(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_EVERY, "Set Mm every") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetMmEveryAttributeName, propertySpecService));
        }
    },
    SetMmTimeout(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_TIMEOUT, "Set Mm timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetMmTimeoutAttributeName, propertySpecService));
        }
    },
    SetMmInstant(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_INSTANT, "Set Mm instant") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetMmInstantAttributeName, propertySpecService));
        }
    },
    SetMmOverflow(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_OVERFLOW, "Set Mm overflow") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetMmOverflowAttributeName, propertySpecService));
        }
    },
    SetMmConfig(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_CONFIG, "Set Mm configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetMmConfigAttributeName, propertySpecService));
        }
    },
    WriteSingleRegisters(DeviceMessageId.MODBUS_CONFIGURATION_WRITE_SINGLE_REGISTERS, "Write single registers") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(RadixFormatAttributeName, true, "DEC", "HEX"));
            propertySpecs.add(propertySpecService.basicPropertySpec(RegisterAddressAttributeName, true, new HexStringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(RegisterValueAttributeName, true, new HexStringFactory()));
        }
    },
    WriteMultipleRegisters(DeviceMessageId.MODBUS_CONFIGURATION_WRITE_MULTIPLE_REGISTERS, "Write multiple registers") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(RadixFormatAttributeName, true, "DEC", "HEX"));
            propertySpecs.add(propertySpecService.basicPropertySpec(RegisterAddressAttributeName, true, new HexStringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(RegisterValueAttributeName, true, new HexStringFactory()));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    ModbusConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return ModbusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
    };

    protected PropertySpec stringProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new StringFactory());
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