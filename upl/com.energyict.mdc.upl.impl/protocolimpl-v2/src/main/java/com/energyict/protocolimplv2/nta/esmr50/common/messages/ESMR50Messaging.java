package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40Messaging;

import java.util.ArrayList;
import java.util.List;

public class ESMR50Messaging extends Dsmr40Messaging {

    public ESMR50Messaging(AbstractMessageExecutor messageExecutor, PropertySpecService propertySpecService,
                           NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor,
                           TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor,
                           LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(messageExecutor, propertySpecService, nlsService, converter, messageFileExtractor, calendarExtractor,
                numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = new ArrayList<>();

        // Activity Calendar - actually used
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_SEND));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND));
        // probably not used
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND));
        supportedMessages.add(this.get(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME));

        // Clock
        supportedMessages.add(this.get(ClockDeviceMessage.SET_TIME));

        // Configuration Change
        supportedMessages.add(this.get(ConfigurationChangeDeviceMessage.ChangeAdministrativeStatus));

        // Device Actions
        // TODO test Low priority, but needs to be implemented (the bitstring should be set to zero for reset via a write); 0.0.97.98.0.255
        supportedMessages.add(this.get(DeviceActionMessage.ALARM_REGISTER_RESET));
        // TODO test Low priority, but needs to be implemented (the bitstring should be set to zero for reset via a write); 0.0.97.97.0.255
        supportedMessages.add(this.get(DeviceActionMessage.ERROR_REGISTER_RESET));

        // Display
        supportedMessages.add(this.get(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1));

        // Firmware - only some can actually be executed
        //      future activation date not supported
        supportedMessages.remove(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE));
        supportedMessages.remove(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER));
        supportedMessages.remove(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER_AND_RESUME));

        supportedMessages.add(this.get(FirmwareDeviceMessage.LTE_MODEM_FIRMWARE_UPGRADE));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER));

        // Load Profile
        supportedMessages.add(this.get(LoadProfileMessage.CONFIGURE_CAPTURE_DEFINITION));
        supportedMessages.add(this.get(LoadProfileMessage.CONFIGURE_CAPTURE_PERIOD));
        supportedMessages.add(this.get(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST));
        supportedMessages.add(this.get(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST));

        // MBUS Configuration
        supportedMessages.add(this.get(MBusConfigurationDeviceMessage.SetMBusConfigBit11));

        // MBUS Setup
        supportedMessages.add(this.get(MBusSetupDeviceMessage.ChangeMBusAttributes));
        supportedMessages.add(this.get(MBusSetupDeviceMessage.MBusClientRemoteCommission));

        // Network and Connectivity
        supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_LTE_APN_NAME));
        supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS));
        supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_LTE_PING_ADDRESS));
        supportedMessages.add(this.get(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS));

        // Security
        supportedMessages.add(this.get(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY));
        supportedMessages.add(this.get(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD));
        supportedMessages.add(this.get(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P0));
        supportedMessages.add(this.get(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P3));
        supportedMessages.add(this.get(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P0));
        supportedMessages.add(this.get(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P3));
        supportedMessages.add(this.get(SecurityMessage.KEY_RENEWAL));

        return supportedMessages;
    }
}
