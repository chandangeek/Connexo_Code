package com.energyict.protocolimplv2.dlms.idis.hs3300.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Date;

public class StandardEventLog {

    private static int STANDARD_EVENT_LOG_ID = 0;
    private static int UNKNOWN_ID = 0;

    public static MeterProtocolEvent buildMeterEvent(Date eventTimeStamp, int eventId) {
        MeterProtocolEvent event;
        switch (eventId) {
            case 1:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.POWERDOWN, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN), "Power down", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 2:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.POWERUP, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERUP), "Power up", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 3:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED), "Daylight saving time enabled or disabled", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 4:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SETCLOCK_BEFORE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SETCLOCK_BEFORE), "Clock adjusted (old date/time)", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 5:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SETCLOCK_AFTER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SETCLOCK_AFTER), "Clock adjusted (new date/time)", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 6:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.CLOCK_INVALID, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.CLOCK_INVALID), "Clock invalid", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 7:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.REPLACE_BATTERY), "Replace Battery", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 8:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.BATTERY_VOLTAGE_LOW, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.BATTERY_VOLTAGE_LOW), "Battery voltage low", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 9:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.TOU_ACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.TOU_ACTIVATED), "TOU activated", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 10:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.ERROR_REGISTER_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.ERROR_REGISTER_CLEARED), "Error register cleared", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 11:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.ALARM_REGISTER_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.ALARM_REGISTER_CLEARED), "Alarm register cleared", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 12:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PROGRAM_MEMORY_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PROGRAM_MEMORY_ERROR), "Program memory error", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 13:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.RAM_MEMORY_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.RAM_MEMORY_ERROR), "RAM  error", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 14:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.NV_MEMORY_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.NV_MEMORY_ERROR), "NV memory error", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 15:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.WATCHDOG_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.WATCHDOG_ERROR), "Watchdog error", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 16:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MEASUREMENT_SYSTEM_ERROR), "Measurement system error", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 17:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.FIRMWARE_READY_FOR_ACTIVATION), "Firmware ready for activation", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 18:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.FIRMWARE_ACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.FIRMWARE_ACTIVATED), "Firmware activated", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 19:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.TOU_PROGRAMMED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.TOU_PROGRAMMED), "Passive TOU programmed", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 20:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.EXTERNAL_ALERT, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.EXTERNAL_ALERT), "External alert detected", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 21:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.END_OF_NONPERIODIC_BILLING_INTERVAL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.END_OF_NONPERIODIC_BILLING_INTERVAL), "End of non-periodic billing interval", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 22:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOADPROFILE_1_CAPTURING_ENABLED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOADPROFILE_1_CAPTURING_ENABLED), "Capturing of load profile 1 enabled", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 23:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOADPROFILE_1_CAPTURING_DISABLED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOADPROFILE_1_CAPTURING_DISABLED), "Capturing of load profile 1 disabled", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 24:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOADPROFILE_2_CAPTURING_ENABLED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOADPROFILE_2_CAPTURING_ENABLED), "Capturing of load profile 2 enabled", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 25:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOADPROFILE_2_CAPTURING_DISABLED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOADPROFILE_2_CAPTURING_DISABLED), "Capturing of load profile 2 disabled", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 30:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.FOTA_UPGRADING_INITIATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.FOTA_UPGRADING_INITIATED), "FOTA upgrading initiated", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 31:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.FOTA_UPGRADING_FINISH_SUCCESS, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.FOTA_UPGRADING_FINISH_SUCCESS), "FOTA upgrading finished successfully", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 32:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.FOTA_UPGRADING_FAILED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.FOTA_UPGRADING_FAILED), "FOTA upgrading failed", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 47:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.CONFIGURATIONCHANGE), "One or more parameters changed", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 48:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.GLOBAL_KEY, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.GLOBAL_KEY), "Global key(s) changed", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 51:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.FIRMWARE_VERIFICATION_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.FIRMWARE_VERIFICATION_FAIL), "FW verification failed", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 52:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.UNEXPECTED_CONSUMPTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.UNEXPECTED_CONSUMPTION), "Unexpected consumption", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 56:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.TARIFF_SCHEME_CHANGED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.TARIFF_SCHEME_CHANGED), "Tariffication scheme changed", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 57:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.START_CERTIFICATION_MODE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.START_CERTIFICATION_MODE), "Start certification mode", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 58:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.END_CERTIFICATION_MODE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.END_CERTIFICATION_MODE), "End certification mode", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 88:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PHASE_REVERSAL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PHASE_REVERSAL), "Phase sequence reversal", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 89:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MISSING_NEUTRAL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MISSING_NEUTRAL), "Missing neutral", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 97:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOAD_MANAGEMENT_PASSIVE_CALENDAR_ACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOAD_MANAGEMENT_PASSIVE_CALENDAR_ACTIVATED), "Load management - passive activity calendar activated", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 98:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOAD_MANAGEMENT_ACTIVITY_CALENDAR_PROGRAMMED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOAD_MANAGEMENT_ACTIVITY_CALENDAR_PROGRAMMED), "Load management - activity calendar programmed", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 99:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.CONSUMER_MESSAGE_UPDATE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.CONSUMER_MESSAGE_UPDATE), "Consumer message update", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 200:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.CONSUMER_INTERFACE_DEACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.CONSUMER_INTERFACE_DEACTIVATED), "Consumer interface de-activated", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 201:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.CONSUMER_INTERFACE_REACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.CONSUMER_INTERFACE_REACTIVATED), "Consumer interface re-activated", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 203:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MODEM_REMOVED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MODEM_REMOVED), "Modem removed", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 204:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PLC_G3_PSK_CHANGE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PLC_G3_PSK_CHANGE), "G3 PSK change", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 205:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOAD_RELAY_CHANGE_REQUEST_REMOTE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOAD_RELAY_CHANGE_REQUEST_REMOTE), "Load relay change request - remote", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 254:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOADPROFILE_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOADPROFILE_CLEARED), "E-meter load profile cleared", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 255:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.EVENT_LOG_CLEARED), "Event log cleared", STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            default:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Unknown event code: " + eventId, STANDARD_EVENT_LOG_ID, UNKNOWN_ID);
        }
        return event;
    }
}
