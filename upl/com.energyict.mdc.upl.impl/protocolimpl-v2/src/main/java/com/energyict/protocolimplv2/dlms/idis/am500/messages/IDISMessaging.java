package com.energyict.protocolimplv2.dlms.idis.am500.messages;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.enums.LoadControlActions;
import com.energyict.protocolimplv2.messages.enums.MonitoredValue;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:34
 */
public class IDISMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    protected List<DeviceMessageSpec> supportedMessages;

    protected IDISMessageExecutor messageExecutor;

    public IDISMessaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new IDISMessageExecutor(getProtocol());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new ArrayList<>();
            supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
            supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);
            supportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ALARM_BITS);
            supportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS);
            supportedMessages.add(AlarmConfigurationMessage.WRITE_ALARM_FILTER);
            supportedMessages.add(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION);
            supportedMessages.add(ContactorDeviceMessage.CLOSE_RELAY);
            supportedMessages.add(ContactorDeviceMessage.OPEN_RELAY);
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
            supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
            supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS);
            supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR);
            supportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1);
            supportedMessages.add(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP2);
            supportedMessages.add(MBusSetupDeviceMessage.Commission);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetTimeoutNotAddressed);
            supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION);
        }
        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> offlineDeviceMessages) {
        return getMessageExecutor().updateSentMessages(offlineDeviceMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)
                || propertySpec.getName().equals(contactorActivationDateAttributeName)
                || propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());     //Epoch
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return convertCodeTableToXML((Code) messageAttribute);
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
            return convertSpecialDaysCodeTableToXML((Code) messageAttribute);
        } else if (propertySpec.getName().equals(configUserFileAttributeName)
                || propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = (UserFile) messageAttribute;
            return ProtocolTools.getHexStringFromBytes(userFile.loadFileInByteArray(), "");  //Bytes of the userFile, as a hex string
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