package com.energyict.protocolimplv2.dlms.idis.am130.messages;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
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

import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:34
 */
public class AM130Messaging extends IDISMessaging {

    public AM130Messaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AM130MessageExecutor(getProtocol());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            addSupportedDeviceMessages(supportedMessages);
        }
        return supportedMessages;
    }

    protected void addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        addCommonDeviceMessages(supportedMessages);
        addAlarmConfigurationMessages(supportedMessages);
        addNetworkConnectivityMessages(supportedMessages);
        addContactorDeviceMessages(supportedMessages);
    }

    protected void addCommonDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS);
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR);
        supportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1);
        supportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP2);
        supportedMessages.add(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION);
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION);
        supportedMessages.add(MBusSetupDeviceMessage.Commission);
    }

    protected void addAlarmConfigurationMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS);
        supportedMessages.add(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER_1_OR_2);
        supportedMessages.add(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER_1_OR_2);
        supportedMessages.add(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2);
        supportedMessages.add(AlarmConfigurationMessage.FULLY_CONFIGURE_PUSH_EVENT_NOTIFICATION);
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS);
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION);
    }

    protected void addContactorDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(ContactorDeviceMessage.CLOSE_RELAY);
        supportedMessages.add(ContactorDeviceMessage.OPEN_RELAY);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
    }

    protected void addNetworkConnectivityMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        supportedMessages.add(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST);
        supportedMessages.add(NetworkConnectivityMessage.ClearWhiteList);
        supportedMessages.add(NetworkConnectivityMessage.SetAutoConnectMode);
        supportedMessages.add(NetworkConnectivityMessage.SetIPAddress);
        supportedMessages.add(NetworkConnectivityMessage.SetSubnetMask);
        supportedMessages.add(NetworkConnectivityMessage.SetGateway);
        supportedMessages.add(NetworkConnectivityMessage.SetUseDHCPFlag);
        supportedMessages.add(NetworkConnectivityMessage.SetPrimaryDNSAddress);
        supportedMessages.add(NetworkConnectivityMessage.SetSecondaryDNSAddress);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (messageAttribute instanceof Password) {
            return ((Password) messageAttribute).getValue();
        }
        return super.format(propertySpec, messageAttribute);
    }
}