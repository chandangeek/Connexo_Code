package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ModbusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WriteModbusCoilMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cisac on 11/12/2015.
 */
public class FP93BMessageConverter extends ModbusMessageConverter {

    public FP93BMessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(propertySpecService, nlsService, converter);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(super.getRegistry());
        registry.put(messageSpec(ModbusConfigurationDeviceMessage.WriteMultipleCoils), new WriteModbusCoilMessage());
        registry.put(messageSpec(ModbusConfigurationDeviceMessage.WriteSingleCoil), new WriteModbusCoilMessage());
        return registry;
    }

}