package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;

import java.util.List;

/**
 * @author sva
 * @since 6/01/2015 - 13:30
 */
public class Dsmr40Messaging extends Dsmr23Messaging {

    public Dsmr40Messaging(AbstractMessageExecutor messageExecutor, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor) {
        super(messageExecutor, propertySpecService, nlsService, converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = super.getSupportedMessages();

        // firmware upgrade related - add message with additional attribute 'Image identifier'
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER));

        // Configuration change
        supportedMessages.add(this.get(ConfigurationChangeDeviceMessage.ChangeAdministrativeStatus));

        if (supportMeterReset) {
            supportedMessages.add(this.get(DeviceActionMessage.RESTORE_FACTORY_SETTINGS));
        }
        if (supportMBus) {
            supportedMessages.add(this.get(ConfigurationChangeDeviceMessage.DISABLE_DISCOVERY_ON_POWER_UP));
            supportedMessages.add(this.get(ConfigurationChangeDeviceMessage.ENABLE_DISCOVERY_ON_POWER_UP));
        }

        // security related
        supportedMessages.add(this.get(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P0));
        supportedMessages.add(this.get(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P3));
        supportedMessages.add(this.get(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P0));
        supportedMessages.add(this.get(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P3));

        // Dsmr 2.3 security related messages not supported in Dmsr 4.0
        supportedMessages.remove(this.get(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL));

        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return super.executePendingMessages(pendingMessages);
    }
}
