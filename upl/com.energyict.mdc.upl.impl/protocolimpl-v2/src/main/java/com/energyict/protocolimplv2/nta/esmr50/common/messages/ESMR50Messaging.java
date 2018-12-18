package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40Messaging;

import java.util.List;

public class ESMR50Messaging extends Dsmr40Messaging {

    public ESMR50Messaging(AbstractMessageExecutor messageExecutor, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(messageExecutor, propertySpecService, nlsService, converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = super.getSupportedMessages();
        //Remove messages


        //LTE modem setup category
        supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_LTE_APN_NAME));
        supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_LTE_PING_ADDRESS));
        //Definable LoadProfile category TODO Verify if these messages should be moved to Dsmr40
        supportedMessages.add(this.get(LoadProfileMessage.CONFIGURE_CAPTURE_DEFINITION));
        supportedMessages.add(this.get(LoadProfileMessage.CONFIGURE_CAPTURE_PERIOD));
        //MBus configuration
        supportedMessages.add(this.get(MBusConfigurationDeviceMessage.SetMBusConfigBit11));

        return supportedMessages;
    }
}
