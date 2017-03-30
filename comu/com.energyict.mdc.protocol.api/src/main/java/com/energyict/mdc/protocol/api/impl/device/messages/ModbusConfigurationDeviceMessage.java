/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

enum ModbusConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetMmEvery(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_EVERY, "Set Mm every") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetMmEveryAttributeName, propertySpecService, thesaurus));
        }
    },
    SetMmTimeout(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_TIMEOUT, "Set Mm timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetMmTimeoutAttributeName, propertySpecService, thesaurus));
        }
    },
    SetMmInstant(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_INSTANT, "Set Mm instant") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetMmInstantAttributeName, propertySpecService, thesaurus));
        }
    },
    SetMmOverflow(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_OVERFLOW, "Set Mm overflow") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetMmOverflowAttributeName, propertySpecService, thesaurus));
        }
    },
    SetMmConfig(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_CONFIG, "Set Mm configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetMmConfigAttributeName, propertySpecService, thesaurus));
        }
    },
    WriteSingleRegisters(DeviceMessageId.MODBUS_CONFIGURATION_WRITE_SINGLE_REGISTERS, "Write single registers") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.RadixFormatAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues("DEC", "HEX")
                            .finish());
            Stream.of(DeviceMessageAttributes.RegisterAddressAttributeName, DeviceMessageAttributes.RegisterValueAttributeName)
                .map(name -> propertySpecService
                                .hexStringSpec()
                                .named(name)
                                .fromThesaurus(thesaurus)
                                .markRequired()
                                .finish())
                .forEach(propertySpecs::add);
        }
    },
    WriteMultipleRegisters(DeviceMessageId.MODBUS_CONFIGURATION_WRITE_MULTIPLE_REGISTERS, "Write multiple registers") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.RadixFormatAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues("DEC", "HEX")
                            .finish());
            Stream.of(DeviceMessageAttributes.RegisterAddressAttributeName, DeviceMessageAttributes.RegisterValueAttributeName)
                    .map(name -> propertySpecService
                            .hexStringSpec()
                            .named(name)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

    protected PropertySpec stringProperty(DeviceMessageAttributes name, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        return propertySpecService.stringSpec().named(name).fromThesaurus(thesaurus).markRequired().finish();
    }

}