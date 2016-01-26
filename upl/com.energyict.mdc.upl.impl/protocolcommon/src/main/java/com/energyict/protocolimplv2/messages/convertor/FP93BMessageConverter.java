package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.messages.ModbusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WriteModbusCoilMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cisac on 11/12/2015.
 */
public class FP93BMessageConverter extends ModbusMessageConverter {
    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(ModbusMessageConverter.registry);

    static {
        registry.put(ModbusConfigurationDeviceMessage.WriteMultipleCoils, new WriteModbusCoilMessage());
        registry.put(ModbusConfigurationDeviceMessage.WriteSingleCoil, new WriteModbusCoilMessage());
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public FP93BMessageConverter() {
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
