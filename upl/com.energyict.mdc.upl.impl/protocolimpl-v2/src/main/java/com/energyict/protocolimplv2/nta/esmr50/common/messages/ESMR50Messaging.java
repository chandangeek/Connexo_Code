package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.*;
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
        // global reset not supported anymore in ESMR
        supportedMessages.remove(this.get(DeviceActionMessage.RESTORE_FACTORY_SETTINGS));
        supportedMessages.remove(this.get(DeviceActionMessage.GLOBAL_METER_RESET));
        supportedMessages.remove(this.get(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS));
        supportedMessages.remove(this.get(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS));
        supportedMessages.remove(this.get(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION));
        supportedMessages.remove(this.get(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM));
        supportedMessages.remove(this.get(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP));
        supportedMessages.remove(this.get(ConfigurationChangeDeviceMessage.ENABLE_DISCOVERY_ON_POWER_UP));
        supportedMessages.remove(this.get(ConfigurationChangeDeviceMessage.DISABLE_DISCOVERY_ON_POWER_UP));
        //TODO check the list of removed messages in com/energyict/protocolimplv2/messages/convertor/Dsmr50MessageConverter.java:82

        //LTE modem setup category
        supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_LTE_APN_NAME));
        supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_LTE_PING_ADDRESS));
        supportedMessages.add(this.get(FirmwareDeviceMessage.LTE_MODEM_FIRMWARE_UPGRADE));

        //MBus configuration
        supportedMessages.add(this.get(MBusConfigurationDeviceMessage.SetMBusConfigBit11));

        return supportedMessages;
    }
}
