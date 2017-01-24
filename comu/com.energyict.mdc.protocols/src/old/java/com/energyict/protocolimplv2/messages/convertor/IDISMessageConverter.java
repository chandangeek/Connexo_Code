package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.device.messages.LoadControlActions;
import com.energyict.mdc.protocol.api.device.messages.MonitoredValue;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivityCalendarMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ContactorControlWithActivationDateAndTimezoneMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SpecialDaysMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.ConfigWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;
import com.energyict.protocols.messaging.DeviceMessageFileStringContentConsumer;
import org.joda.time.DateTimeConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

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
        } else if (propertySpec.getName().equals(activityCalendarAttributeName)) {
            return convertCodeTableToXML((Calendar) messageAttribute);
        } else if (propertySpec.getName().equals(specialDaysAttributeName)) {
            return convertSpecialDaysCodeTableToXML((Calendar) messageAttribute);
        } else if (propertySpec.getName().equals(resumeFirmwareUpdateAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(configUserFileAttributeName)) {
            return DeviceMessageFileStringContentConsumer.readFrom((DeviceMessageFile) messageAttribute, "UTF-8");  //Return the content of the file, should be ASCII (XML)
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
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
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(TIME_OUT_NOT_ADDRESSEDAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds() / DateTimeConstants.SECONDS_PER_MINUTE);
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new ActivityCalendarMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarAttributeName));
        registry.put(DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND, new SpecialDaysMessageEntry(specialDaysAttributeName));

        registry.put(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS, new SimpleTagMessageEntry("ResetAllAlarmBits"));
        registry.put(DeviceMessageId.ALARM_CONFIGURATION_WRITE_ALARM_FILTER, new MultipleAttributeMessageEntry("WriteAlarmFilter", "Alarm filter (decimal value)"));
        registry.put(DeviceMessageId.GENERAL_WRITE_FULL_CONFIGURATION, new ConfigWithUserFileMessageEntry(configUserFileAttributeName, "Configuration download"));

        registry.put(DeviceMessageId.CONTACTOR_CLOSE_RELAY, new MultipleAttributeMessageEntry("CloseRelay", "Relay number (1 or 2)"));
        registry.put(DeviceMessageId.CONTACTOR_OPEN_RELAY, new MultipleAttributeMessageEntry("OpenRelay", "Relay number (1 or 2)"));
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new SimpleTagMessageEntry("RemoteDisconnect"));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new SimpleTagMessageEntry("RemoteConnect"));
        registry.put(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, new ContactorControlWithActivationDateAndTimezoneMessageEntry("TimedDisconnect"));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, new ContactorControlWithActivationDateAndTimezoneMessageEntry("TimedReconnect"));
        registry.put(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE, new MultipleAttributeMessageEntry("SetControlMode", "Control mode (range 0 - 6)"));

        registry.put(DeviceMessageId.LOAD_BALANCING_CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS, new MultipleAttributeMessageEntry("LoadControlledConnect", getLimiterAttributes()));
        registry.put(DeviceMessageId.LOAD_BALANCING_CONFIGURE_SUPERVISION_MONITOR, new MultipleAttributeMessageEntry("SuperVision", "Phase (1, 2 or 3)", " Threshold (ampere)"));

        registry.put(DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP1, new MultipleAttributeMessageEntry("WriteLP1CapturePeriod", "Capture period (seconds)"));
        registry.put(DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP2, new MultipleAttributeMessageEntry("WriteLP2CapturePeriod", "Capture period (seconds)"));
        //TODO: write LP captured objects, this uses optional property specs

        registry.put(DeviceMessageId.MBUS_SETUP_COMMISSION, new SimpleTagMessageEntry("SlaveCommission"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_TIMEOUT_NOT_ADDRESSED, new MultipleAttributeMessageEntry("SetTimeOutNotAddressed", "timeout_not_addressed"));

        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateFileAttributeName, resumeFirmwareUpdateAttributeName));
        return registry;
    }

    private String[] getLimiterAttributes() {
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

}