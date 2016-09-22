package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.messages.ModbusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WriteModbusCoilMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cisac on 11/18/2015.
 */
public class VLM20MessageConverter extends AbstractMessageConverter{

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ModbusConfigurationDeviceMessage.WriteSingleCoil, new WriteModbusCoilMessage());
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    public VLM20MessageConverter(){
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }
}
