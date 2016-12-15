package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the legacy IDIS protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class IDISMessageConverter extends AbstractMessageConverter {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm");


    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {

        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new ActivityCalendarMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND, new SpecialDaysMessageEntry(specialDaysCodeTableAttributeName));

        registry.put(AlarmConfigurationMessage.RESET_ALL_ALARM_BITS, new SimpleTagMessageEntry("ResetAllAlarmBits"));
        registry.put(AlarmConfigurationMessage.RESET_ALL_ERROR_BITS, new SimpleTagMessageEntry("ResetAllErrorBits"));
        registry.put(AlarmConfigurationMessage.WRITE_ALARM_FILTER, new MultipleAttributeMessageEntry("WriteAlarmFilter", "Alarm filter (decimal value)"));
        registry.put(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION, new ConfigWithUserFileMessageEntry(configUserFileAttributeName, "Configuration download"));

        registry.put(ContactorDeviceMessage.CLOSE_RELAY, new MultipleAttributeMessageEntry("CloseRelay", "Relay number (1 or 2)"));
        registry.put(ContactorDeviceMessage.OPEN_RELAY, new MultipleAttributeMessageEntry("OpenRelay", "Relay number (1 or 2)"));
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new SimpleTagMessageEntry("RemoteDisconnect"));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new SimpleTagMessageEntry("RemoteConnect"));
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, new ContactorControlWithActivationDateAndTimezoneMessageEntry("TimedDisconnect"));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, new ContactorControlWithActivationDateAndTimezoneMessageEntry("TimedReconnect"));
        registry.put(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE, new MultipleAttributeMessageEntry("SetControlMode", "Control mode (range 0 - 6)"));

        registry.put(LoadBalanceDeviceMessage.CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS, new MultipleAttributeMessageEntry("LoadControlledConnect", getLimiterAttributes()));
        registry.put(LoadBalanceDeviceMessage.CONFIGURE_SUPERVISION_MONITOR, new MultipleAttributeMessageEntry("SuperVision", "Phase (1, 2 or 3)", " Threshold (ampere)"));

        registry.put(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1, new MultipleAttributeMessageEntry("WriteLP1CapturePeriod", "Capture period (seconds)"));
        registry.put(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP2, new MultipleAttributeMessageEntry("WriteLP2CapturePeriod", "Capture period (seconds)"));
        //TODO: write LP captured objects, this uses optional property specs

        registry.put(MBusSetupDeviceMessage.Commission, new SimpleTagMessageEntry("SlaveCommission"));
        registry.put(PLCConfigurationDeviceMessage.SetTimeoutNotAddressed, new MultipleAttributeMessageEntry("SetTimeOutNotAddressed", "timeout_not_addressed"));

        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName, resumeFirmwareUpdateAttributeName));
    }

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

    /**
     * Default constructor for at-runtime instantiation
     */
    public IDISMessageConverter() {
        super();
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
            simpleDateFormat.setTimeZone(TimeZone.getDefault());  //Use system timezone
            return simpleDateFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return convertCodeTableToXML((Code) messageAttribute);
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
            return convertSpecialDaysCodeTableToXML((Code) messageAttribute);
        } else if (propertySpec.getName().equals(resumeFirmwareUpdateAttributeName)) {
            return ((Boolean) messageAttribute).toString();
        } else if (propertySpec.getName().equals(configUserFileAttributeName)
                || propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = (UserFile) messageAttribute;
            return new String(userFile.loadFileInByteArray());  //Bytes of the userFile, as a string
        } else if (propertySpec.getName().equals(monitoredValueAttributeName)) {
            return String.valueOf(MonitoredValue.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(actionWhenUnderThresholdAttributeName)) {
            return String.valueOf(LoadControlActions.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(actionWhenOverThresholdAttributeName)) {
            return String.valueOf(LoadControlActions.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(emergencyProfileActivationDateAttributeName)) {
            return europeanDateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)
                || (propertySpec.getName().equals(capturePeriodAttributeName))
                || (propertySpec.getName().equals(underThresholdDurationAttributeName))
                || (propertySpec.getName().equals(emergencyProfileDurationAttributeName))) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(TIME_OUT_NOT_ADDRESSEDAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds() / 60);  //Minutes
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}