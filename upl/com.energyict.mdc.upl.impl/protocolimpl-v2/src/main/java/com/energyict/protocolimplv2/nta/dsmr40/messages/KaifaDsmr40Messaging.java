package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.util.List;

public class KaifaDsmr40Messaging extends Dsmr40Messaging {
    public KaifaDsmr40Messaging(AbstractMessageExecutor messageExecutor, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(messageExecutor, propertySpecService, nlsService, converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = super.getSupportedMessages();

        supportedMessages.add(this.get(PowerConfigurationDeviceMessage.SetVoltageSagThreshold));
        supportedMessages.add(this.get(PowerConfigurationDeviceMessage.SetVoltageSagTimeThreshold));
        supportedMessages.add(this.get(PowerConfigurationDeviceMessage.SetVoltageSwellThreshold));
        supportedMessages.add(this.get(PowerConfigurationDeviceMessage.SetVoltageSwellTimeThreshold));



        return supportedMessages;
    }
}
