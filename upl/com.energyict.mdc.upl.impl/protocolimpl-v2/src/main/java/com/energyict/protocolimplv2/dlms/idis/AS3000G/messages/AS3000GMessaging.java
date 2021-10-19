package com.energyict.protocolimplv2.dlms.idis.AS3000G.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.util.List;

public class AS3000GMessaging extends AM540Messaging {

    public AS3000GMessaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AS3000GMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    protected List<DeviceMessageSpec> addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(DeviceActionMessage.BILLING_RESET.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(DeviceActionMessage.BillingDateConfiguration.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadBalanceDeviceMessage.UPDATE_SUPERVISION_MONITOR.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_EXCEPT_EMERGENCY_ONES.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_ATTRIBUTES_4TO9.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP2.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadProfileMessage.LOAD_PROFILE_OPT_IN_OUT.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadProfileMessage.SET_DISPLAY_ON_OFF.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadProfileMessage.WRITE_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(LogBookDeviceMessage.ResetSecurityGroupEventCounterObjects.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LogBookDeviceMessage.ResetAllSecurityGroupEventCounters.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));


        addContactorDeviceMessages(supportedMessages);

        return supportedMessages;
    }

}
