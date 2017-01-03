package com.energyict.protocolimplv2.dlms.idis.am130.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:34
 */
public class AM130Messaging extends IDISMessaging {

    public AM130Messaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, messageFileExtractor);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AM130MessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return addSupportedDeviceMessages(new ArrayList<>());
    }

    protected List<DeviceMessageSpec> addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        addCommonDeviceMessages(supportedMessages);
        addAlarmConfigurationMessages(supportedMessages);
        addNetworkConnectivityMessages(supportedMessages);
        addContactorDeviceMessages(supportedMessages);
        return supportedMessages;
    }

    protected void addCommonDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP2.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(MBusSetupDeviceMessage.Commission.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
    }

    protected void addAlarmConfigurationMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER_1_OR_2.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER_1_OR_2.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.FULLY_CONFIGURE_PUSH_EVENT_NOTIFICATION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
    }

    protected void addContactorDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(ContactorDeviceMessage.CLOSE_RELAY.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.OPEN_RELAY.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
    }

    protected void addNetworkConnectivityMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(NetworkConnectivityMessage.ClearWhiteList.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(NetworkConnectivityMessage.SetAutoConnectMode.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(NetworkConnectivityMessage.SetIPAddress.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(NetworkConnectivityMessage.SetSubnetMask.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(NetworkConnectivityMessage.SetGateway.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(NetworkConnectivityMessage.SetUseDHCPFlag.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(NetworkConnectivityMessage.SetPrimaryDNSAddress.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(NetworkConnectivityMessage.SetSecondaryDNSAddress.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (messageAttribute instanceof Password) {
            return ((Password) messageAttribute).getValue();
        }
        return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }
}