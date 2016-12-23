package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalender;

import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivityCalendarMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ContactorControlWithActivationDateAndTimezoneMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SpecialDaysMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.ConfigWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.enums.LoadControlActions;
import com.energyict.protocolimplv2.messages.enums.MonitoredValue;
import com.google.common.collect.ImmutableMap;

import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.actionWhenUnderThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.alarmFilterAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.configUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitoredValueAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.phaseAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.thresholdInAmpereAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.underThresholdDurationAttributeName;

/**
 * Represents a MessageConverter for the legacy IDIS protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class IDISMessageConverter extends AbstractMessageConverter {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/mm/yyyy hh:mm");

    private final Extractor extractor;

    private static String[] getLimiterAttributes() {
        String[] result = new String[10];
        result[0] = "Monitored value (1: Total inst. current, 2: Avg A+ (sliding demand), 3: Avg total A (sliding demand))";
        result[1] = "Normal threshold";
        result[2] = "Emergency threshold";
        result[3] = "Minimal over threshold duration (seconds)";
        result[4] = "Minimal under threshold duration (seconds)";
        result[5] = "Emergency profile ID";
        result[6] = "Emergency activation time (dd/mm/yyyy hh:mm:ss)";
        result[7] = "Emergency duration (seconds)";
        result[8] = "Emergency profile group id list (comma separated, e.g. 1,2,3)";
        result[9] = "Action when under threshold (0: nothing, 2: reconnect)";
        return result;
    }

    public IDISMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.extractor = extractor;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(activityCalendarNameAttributeName)
                || propertySpec.getName().equals(contactorModeAttributeName)
                || propertySpec.getName().equals(relayNumberAttributeName)
                || propertySpec.getName().equals(normalThresholdAttributeName)
                || propertySpec.getName().equals(emergencyThresholdAttributeName)
                || propertySpec.getName().equals(thresholdInAmpereAttributeName)
                || propertySpec.getName().equals(phaseAttributeName)
                || propertySpec.getName().equals(emergencyProfileIdAttributeName)
                || propertySpec.getName().equals(emergencyProfileGroupIdListAttributeName)
                || propertySpec.getName().equals(alarmFilterAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getDefault());  //Use system timezone
            return SIMPLE_DATE_FORMAT.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return convertCodeTableToXML((TariffCalender) messageAttribute);
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
            return convertSpecialDaysCodeTableToXML((TariffCalender) messageAttribute);
        } else if (propertySpec.getName().equals(resumeFirmwareUpdateAttributeName)) {
            return ((Boolean) messageAttribute).toString();
        } else if (propertySpec.getName().equals(configUserFileAttributeName)
                || propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            return this.extractor.contents((DeviceMessageFile) messageAttribute);  //Bytes of the userFile, as a string
        } else if (propertySpec.getName().equals(monitoredValueAttributeName)) {
            return String.valueOf(MonitoredValue.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(actionWhenUnderThresholdAttributeName)) {
            return String.valueOf(LoadControlActions.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)) {
            return europeanDateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)
                || (propertySpec.getName().equals(capturePeriodAttributeName))
                || (propertySpec.getName().equals(underThresholdDurationAttributeName))
                || (propertySpec.getName().equals(emergencyProfileDurationAttributeName))) {
            return String.valueOf(Temporals.toSeconds((TemporalAmount) messageAttribute));
        } else if (propertySpec.getName().equals(TIME_OUT_NOT_ADDRESSEDAttributeName)) {
            return String.valueOf(Temporals.toSeconds((TemporalAmount) messageAttribute) / 60);  //Minutes
        }
        return EMPTY_FORMAT;
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME), new ActivityCalendarMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName))
                .put(messageSpec(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND), new SpecialDaysMessageEntry(specialDaysCodeTableAttributeName))
                .put(messageSpec(AlarmConfigurationMessage.RESET_ALL_ALARM_BITS), new SimpleTagMessageEntry("ResetAllAlarmBits"))
                .put(messageSpec(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS), new SimpleTagMessageEntry("ResetAllErrorBits"))
                .put(messageSpec(AlarmConfigurationMessage.WRITE_ALARM_FILTER), new MultipleAttributeMessageEntry("WriteAlarmFilter", "Alarm filter (decimal value)"))
                .put(messageSpec(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION), new ConfigWithUserFileMessageEntry(configUserFileAttributeName, "Configuration download"))
                .put(messageSpec(ContactorDeviceMessage.CLOSE_RELAY), new MultipleAttributeMessageEntry("CloseRelay", "Relay number (1 or 2)"))
                .put(messageSpec(ContactorDeviceMessage.OPEN_RELAY), new MultipleAttributeMessageEntry("OpenRelay", "Relay number (1 or 2)"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new SimpleTagMessageEntry("RemoteDisconnect"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new SimpleTagMessageEntry("RemoteConnect"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE), new ContactorControlWithActivationDateAndTimezoneMessageEntry("TimedDisconnect"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE), new ContactorControlWithActivationDateAndTimezoneMessageEntry("TimedReconnect"))
                .put(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE), new MultipleAttributeMessageEntry("SetControlMode", "Control mode (range 0 - 6)"))
                .put(messageSpec(LoadBalanceDeviceMessage.CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS), new MultipleAttributeMessageEntry("LoadControlledConnect", getLimiterAttributes()))
                .put(messageSpec(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR), new MultipleAttributeMessageEntry("SuperVision", "Phase (1, 2 or 3)", " Threshold (ampere)"))
                .put(messageSpec(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1), new MultipleAttributeMessageEntry("WriteLP1CapturePeriod", "Capture period (seconds)"))
                .put(messageSpec(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP2), new MultipleAttributeMessageEntry("WriteLP2CapturePeriod", "Capture period (seconds)"))
                .put(messageSpec(MBusSetupDeviceMessage.Commission), new SimpleTagMessageEntry("SlaveCommission"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetTimeoutNotAddressed), new MultipleAttributeMessageEntry("SetTimeOutNotAddressed", "timeout_not_addressed"))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION), new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName, resumeFirmwareUpdateAttributeName))
                .build();
    }

}