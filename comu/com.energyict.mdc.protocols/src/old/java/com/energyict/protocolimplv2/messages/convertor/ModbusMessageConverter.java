/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.messages.WriteModbusRegisterMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter that maps the new Modbus messages to legacy XML
 *
 * @author sva
 * @since 24/10/13 - 9:38
 */
public class ModbusMessageConverter extends AbstractMessageConverter {

    /**
     * Default constructor for at-runtime instantiation
     */
    public ModbusMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.MODBUS_CONFIGURATION_WRITE_SINGLE_REGISTERS, new WriteModbusRegisterMessage());
        registry.put(DeviceMessageId.MODBUS_CONFIGURATION_WRITE_MULTIPLE_REGISTERS, new WriteModbusRegisterMessage());
        return registry;
    }

}