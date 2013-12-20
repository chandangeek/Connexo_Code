package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.messages.ModbusConfigurationDeviceMessage;
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
     * Represents a mapping between {@link DeviceMessageSpec}s
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ModbusConfigurationDeviceMessage.WriteSingleRegisters, new WriteModbusRegisterMessage());
        registry.put(ModbusConfigurationDeviceMessage.WriteMultipleRegisters, new WriteModbusRegisterMessage());
    }

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

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
