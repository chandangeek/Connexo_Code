package com.energyict.protocolimplv2.dlms.idis.am130.messages;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.messages.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:34
 */
public class AM130Messaging extends IDISMessaging {

    private final static List<DeviceMessageSpec> am130SupportedMessages;

    static {
        am130SupportedMessages = new ArrayList<>();

        am130SupportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        am130SupportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);

        am130SupportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS);
        am130SupportedMessages.add(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER_1_OR_2);
        am130SupportedMessages.add(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER_1_OR_2);
        am130SupportedMessages.add(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2);
        am130SupportedMessages.add(AlarmConfigurationMessage.FULLY_CONFIGURE_PUSH_EVENT_NOTIFICATION);

        am130SupportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);
        am130SupportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);

        am130SupportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        am130SupportedMessages.add(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST);
        am130SupportedMessages.add(NetworkConnectivityMessage.ClearWhiteList);
        am130SupportedMessages.add(NetworkConnectivityMessage.SetAutoConnectMode);

        am130SupportedMessages.add(NetworkConnectivityMessage.SetIPAddress);
        am130SupportedMessages.add(NetworkConnectivityMessage.SetSubnetMask);
        am130SupportedMessages.add(NetworkConnectivityMessage.SetGateway);
        am130SupportedMessages.add(NetworkConnectivityMessage.SetUseDHCPFlag);
        am130SupportedMessages.add(NetworkConnectivityMessage.SetPrimaryDNSAddress);
        am130SupportedMessages.add(NetworkConnectivityMessage.SetSecondaryDNSAddress);

        am130SupportedMessages.add(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION);
        am130SupportedMessages.add(ContactorDeviceMessage.CLOSE_RELAY);
        am130SupportedMessages.add(ContactorDeviceMessage.OPEN_RELAY);
        am130SupportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        am130SupportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
        am130SupportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        am130SupportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        am130SupportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
        am130SupportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS);
        am130SupportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR);
        am130SupportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1);
        am130SupportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP2);
        am130SupportedMessages.add(MBusSetupDeviceMessage.Commission);
        am130SupportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION);
    }

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
        return am130SupportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (messageAttribute instanceof Password) {
            return ((Password) messageAttribute).getValue();
        }
        return super.format(propertySpec, messageAttribute);
    }
}