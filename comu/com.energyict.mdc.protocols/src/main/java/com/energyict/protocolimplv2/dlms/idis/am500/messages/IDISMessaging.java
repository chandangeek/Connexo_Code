/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am500.messages;


import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.LoadControlActions;
import com.energyict.mdc.protocol.api.device.messages.MonitoredValue;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.actionWhenUnderThresholdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.configUserFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.monitoredValueAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.underThresholdDurationAttributeName;

public class IDISMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    protected Set<DeviceMessageId> supportedMessages = EnumSet.of(
//            DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME,
//            DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND,
            DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS,
            DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS,
            DeviceMessageId.ALARM_CONFIGURATION_WRITE_ALARM_FILTER,
//            DeviceMessageId.GENERAL_WRITE_FULL_CONFIGURATION,
            DeviceMessageId.CONTACTOR_CLOSE_RELAY,
            DeviceMessageId.CONTACTOR_OPEN,
            DeviceMessageId.CONTACTOR_CLOSE,
            DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE,
            DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE,
            DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE,
            DeviceMessageId.LOAD_BALANCING_CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS,
            DeviceMessageId.LOAD_BALANCING_CONFIGURE_SUPERVISION_MONITOR,
            DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP1,
            DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP2,
            DeviceMessageId.MBUS_SETUP_COMMISSION,
            DeviceMessageId.PLC_CONFIGURATION_SET_TIMEOUT_NOT_ADDRESSED
//            DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE
    );

    protected IDISMessageExecutor messageExecutor;

    public IDISMessaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new IDISMessageExecutor(getProtocol(), getProtocol().getIssueService(), getProtocol().getReadingTypeUtilService(), getProtocol().getCollectedDataFactory());
        }
        return messageExecutor;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageExecutor().updateSentMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)
                || propertySpec.getName().equals(contactorActivationDateAttributeName)
                || propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());     //Epoch
//        } else if (propertySpec.getName().equals(activityCalendarNameAttributeName)) {
//            return convertCodeTableToXML((Code) messageAttribute);
//        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
//            return convertSpecialDaysCodeTableToXML((Code) messageAttribute);
        } else if (propertySpec.getName().equals(configUserFileAttributeName)
                || propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
//            UserFile userFile = (UserFile) messageAttribute;
//            return ProtocolTools.getHexStringFromBytes(userFile.loadFileInByteArray(), "");  //Bytes of the userFile, as a hex string
        } else if (propertySpec.getName().equals(monitoredValueAttributeName)) {
            return String.valueOf(MonitoredValue.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(actionWhenUnderThresholdAttributeName)) {
            return String.valueOf(LoadControlActions.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)
                || (propertySpec.getName().equals(capturePeriodAttributeName))
                || (propertySpec.getName().equals(underThresholdDurationAttributeName))
                || (propertySpec.getName().equals(emergencyProfileDurationAttributeName))) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(TIME_OUT_NOT_ADDRESSEDAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds() / 60);  //Minutes
        }
        return messageAttribute.toString();
    }

}