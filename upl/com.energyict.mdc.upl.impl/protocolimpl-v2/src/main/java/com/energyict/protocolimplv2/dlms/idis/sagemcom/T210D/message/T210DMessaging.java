package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message;

import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.messages.*;

import java.util.List;

/**
 * Created by cisac on 8/1/2016.
 */
public class T210DMessaging extends AM540Messaging {

    public T210DMessaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new T210DMessageExecutor(getProtocol());
        }
        return messageExecutor;
    }

    @Override
    protected void addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        //Security
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);
        //Supervision monitor
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR_FOR_IMPORT_EXPORT);
        //FW messages
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION);
        supportedMessages.add(FirmwareDeviceMessage.VerifyAndActivateFirmware);
        //Alarms
        supportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS);
        supportedMessages.add(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER);
        supportedMessages.add(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER);
        supportedMessages.add(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER);
        //Configuration for push setup objects
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS);
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION);
        //Configuration for ConfigureGeneralLocalPortReadout captured_objects
        supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureGeneralLocalPortReadout);
        //Contactor
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);

//         To implement and test in V2 of the prototype device
//        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
//        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);
//        supportedMessages.add(MBusSetupDeviceMessage.Commission);
    }

}
