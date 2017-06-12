package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.messaging.xmlparser.XMLtoAXDRParser;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dayProfileXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.seasonProfileXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.weekProfileXmlUserFileAttributeName;

/**
 * Created by cisac on 8/1/2016.
 */
public class T210DMessaging extends AM540Messaging {

    public T210DMessaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new T210DMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    protected List<DeviceMessageSpec> addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        //Security
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
//        supportedMessages.add(SecurityMessage.SET_REQUIRED_PROTECTION_FOR_DATA_PROTECTION_SETUP.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        //Supervision monitor
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR_FOR_IMPORT_EXPORT.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        //FW messages
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER_AND_RESUME.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.VerifyAndActivateFirmware.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.VerifyAndActivateFirmwareAtGivenDate.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.FIRMWARE_IMAGE_ACTIVATION_WITH_DATA_PROTECTION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.FIRMWARE_IMAGE_ACTIVATION_WITH_DATA_PROTECTION_AND_ACTIVATION_DATE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        //Alarms
        supportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        //Configuration for push setup objects
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        //Configuration for ConfigureGeneralLocalPortReadout captured_objects
        supportedMessages.add(ConfigurationChangeDeviceMessage.DISABLE_PUSH_ON_INSTALLATION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ConfigurationChangeDeviceMessage.ENABLE_PUSH_ON_INTERVAL_OBJECTS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ConfigurationChangeDeviceMessage.ENABLE_PUSH_ON_INTERVAL_OBJECTS_WITH_TIME_DATE_ARRAY.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureGeneralLocalPortReadout.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        //Contactor
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_ACTION_WITH_ACTIVATION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_DATA_PROTECTION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_DATA_PROTECTION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.REMOTE_DISCONNECT_WITH_DATA_PROTECTION_AND_ACTIVATION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ContactorDeviceMessage.REMOTE_CONNECT_WITH_DATA_PROTECTION_AND_ACTIVATION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDAR_WITH_DATETIME_FROM_XML.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_WITH_GIVEN_TABLE_OBIS_FROM_XML.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(MBusSetupDeviceMessage.ScanAndInstallWiredMbusDevices.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(MBusSetupDeviceMessage.InstallWirelessMbusDevices.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(MBusSetupDeviceMessage.ScanAndInstallWiredMbusDeviceForGivenMeterIdentification.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(MBusSetupDeviceMessage.InstallWirelessMbusDeviceForGivenMeterIdentification.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(dayProfileXmlUserFileAttributeName)
                || propertySpec.getName().equals(weekProfileXmlUserFileAttributeName)
                || propertySpec.getName().equals(seasonProfileXmlUserFileAttributeName)
                || propertySpec.getName().equals(specialDaysXmlUserFileAttributeName)) {


            DeviceMessageFile userFile = (DeviceMessageFile) messageAttribute;

            XMLtoAXDRParser xmlToAXDRParser = new XMLtoAXDRParser();
            AbstractDataType abstractDataType = xmlToAXDRParser.parseXml(this.messageFileExtractor.binaryContents(userFile));
            return ProtocolTools.getHexStringFromBytes(abstractDataType.getBEREncodedByteArray(), "");
        }
        return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

}