/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am130.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;

import java.util.HashSet;
import java.util.Set;

public class AM130Messaging extends IDISMessaging {

    public AM130Messaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }


    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AM130MessageExecutor(getProtocol(), getProtocol().getIssueService(), getProtocol().getReadingTypeUtilService(), getProtocol().getCollectedDataFactory());
        }
        return messageExecutor;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new HashSet<>();
            addSupportedDeviceMessages(supportedMessages);
        }
        return supportedMessages;
    }

    protected void addSupportedDeviceMessages(Set<DeviceMessageId> supportedMessages) {
        addCommonDeviceMessages(supportedMessages);
        addAlarmConfigurationMessages(supportedMessages);
        addNetworkConnectivityMessages(supportedMessages);
        addContactorDeviceMessages(supportedMessages);
    }

    protected void addCommonDeviceMessages(Set<DeviceMessageId> supportedMessages) {
        supportedMessages.add(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);
        supportedMessages.add(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);
        supportedMessages.add(DeviceMessageId.LOAD_BALANCING_CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS);
        supportedMessages.add(DeviceMessageId.LOAD_BALANCING_CONFIGURE_SUPERVISION_MONITOR);
        supportedMessages.add(DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP1);
        supportedMessages.add(DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP2);
        supportedMessages.add(DeviceMessageId.GENERAL_WRITE_FULL_CONFIGURATION);
        supportedMessages.add(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        supportedMessages.add(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND);
        supportedMessages.add(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE);
        supportedMessages.add(DeviceMessageId.MBUS_SETUP_COMMISSION);
    }

    protected void addAlarmConfigurationMessages(Set<DeviceMessageId> supportedMessages) {
        supportedMessages.add(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS);
//        supportedMessages.add(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER_1_OR_2);
//        supportedMessages.add(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER_1_OR_2);
//        supportedMessages.add(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2);
//        supportedMessages.add(AlarmConfigurationMessage.FULLY_CONFIGURE_PUSH_EVENT_NOTIFICATION);
//        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS);
//        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION);
    }

    protected void addContactorDeviceMessages(Set<DeviceMessageId> supportedMessages) {
        supportedMessages.add(DeviceMessageId.CONTACTOR_CLOSE_RELAY);
        supportedMessages.add(DeviceMessageId.CONTACTOR_OPEN_RELAY);
        supportedMessages.add(DeviceMessageId.CONTACTOR_OPEN);
        supportedMessages.add(DeviceMessageId.CONTACTOR_CLOSE);
        supportedMessages.add(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        supportedMessages.add(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        supportedMessages.add(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE);
    }

    protected void addNetworkConnectivityMessages(Set<DeviceMessageId> supportedMessages) {
        supportedMessages.add(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS);
        supportedMessages.add(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_PHONENUMBERS_TO_WHITE_LIST);
        supportedMessages.add(DeviceMessageId.NETWORK_CONNECTIVITY_CLEAR_WHITE_LIST);
//        supportedMessages.add(NetworkConnectivityMessage.SetAutoConnectMode);
        supportedMessages.add(DeviceMessageId.NETWORK_CONNECTIVITY_SET_IP_ADDRESS);
        supportedMessages.add(DeviceMessageId.NETWORK_CONNECTIVITY_SET_SUBNET_MASK);
        supportedMessages.add(DeviceMessageId.NETWORK_CONNECTIVITY_SET_GATEWAY);
//        supportedMessages.add(NetworkConnectivityMessage.SetUseDHCPFlag);
//        supportedMessages.add(NetworkConnectivityMessage.SetPrimaryDNSAddress);
//        supportedMessages.add(NetworkConnectivityMessage.SetSecondaryDNSAddress);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (messageAttribute instanceof Password) {
            return ((Password) messageAttribute).getValue();
        }
        return super.format(propertySpec, messageAttribute);
    }
}