package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;

import java.util.Map;

/**
 * Created by cisac on 8/6/2015.
 */
public class XemexWatchTalkMbusMessageConverter extends Dsmr23MBusDeviceMessageConverter{

    public XemexWatchTalkMbusMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = super.getRegistry();
        // Disconnect control
        registry.remove(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE));
        // MBus setup
        registry.remove(messageSpec(MBusSetupDeviceMessage.UseCorrectedValues));
        registry.remove(messageSpec(MBusSetupDeviceMessage.UseUncorrectedValues));
        // LoadProfiles
        registry.remove(messageSpec(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST));
        registry.remove(messageSpec(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST));
        return registry;
    }

}
