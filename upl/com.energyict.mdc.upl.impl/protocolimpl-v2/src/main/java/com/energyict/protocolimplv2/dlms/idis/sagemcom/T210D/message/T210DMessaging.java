package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message;

import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.messaging.xmlparser.XMLtoAXDRParser;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.messages.*;

import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

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
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);
//        supportedMessages.add(SecurityMessage.SET_REQUIRED_PROTECTION_FOR_DATA_PROTECTION_SETUP);
        //Supervision monitor
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR_FOR_IMPORT_EXPORT);
        //FW messages
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER);
        supportedMessages.add(FirmwareDeviceMessage.VerifyAndActivateFirmware);
        supportedMessages.add(FirmwareDeviceMessage.FIRMWARE_IMAGE_ACTIVATION_WITH_DATA_PROTECTION);
        //Alarms
        supportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS);
        supportedMessages.add(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER);
        supportedMessages.add(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER);
        supportedMessages.add(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER);
        //Configuration for push setup objects
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS);
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION);
        //Configuration for ConfigureGeneralLocalPortReadout captured_objects
        supportedMessages.add(ConfigurationChangeDeviceMessage.DISABLE_PUSH_ON_INSTALLATION);
        supportedMessages.add(ConfigurationChangeDeviceMessage.ENABLE_PUSH_ON_INTERVAL_OBJECTS);
        supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureGeneralLocalPortReadout);
        //Contactor
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_ACTION_WITH_ACTIVATION);
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_DATA_PROTECTION);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_DATA_PROTECTION);

        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDAR_WITH_DATETIME_FROM_XML);
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_WITH_GIVEN_TABLE_OBIS_FROM_XML);
        supportedMessages.add(MBusSetupDeviceMessage.ScanAndInstallWiredMbusDevices);
        supportedMessages.add(MBusSetupDeviceMessage.InstallWirelessMbusDevices);
        supportedMessages.add(MBusSetupDeviceMessage.ScanAndInstallWiredMbusDeviceForGivenMeterIdentification);
        supportedMessages.add(MBusSetupDeviceMessage.InstallWirelessMbusDeviceForGivenMeterIdentification);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(dayProfileXmlUserFileAttributeName)
                || propertySpec.getName().equals(weekProfileXmlUserFileAttributeName)
                || propertySpec.getName().equals(seasonProfileXmlUserFileAttributeName)
                || propertySpec.getName().equals(specialDaysXmlUserFileAttributeName)) {
            UserFile userFile = (UserFile) messageAttribute;
            XMLtoAXDRParser xmlToAXDRParser = new XMLtoAXDRParser();
            AbstractDataType abstractDataType = xmlToAXDRParser.parseXml(userFile.loadFileInByteArray());
            return ProtocolTools.getHexStringFromBytes(abstractDataType.getBEREncodedByteArray(), "");
        }
        return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

}
