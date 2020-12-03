package com.energyict.protocolimplv2.dlms.idis.hs3300.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Date;

public class SecurityEventLog {

    private static int SECURITY_EVENT_LOG_ID = 9;
    private static int UNKNOWN_ID = 0;

    public static MeterProtocolEvent buildMeterEvent(Date eventTimeStamp, int eventId) {
        MeterProtocolEvent event;
        switch (eventId) {
            case 1:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.POWERDOWN, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN), "Power down", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 2:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.POWERUP, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERUP), "Power up", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 4:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SETCLOCK_BEFORE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SETCLOCK_BEFORE ), "Clock adjusted (old date/time)", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 5:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SETCLOCK_AFTER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SETCLOCK_AFTER), "Clock adjusted (new date/time)", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 10:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.ERROR_REGISTER_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.ERROR_REGISTER_CLEARED), "Error register cleared", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 11:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.ALARM_REGISTER_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.ALARM_REGISTER_CLEARED), "Alarm register cleared", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 12:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PROGRAM_MEMORY_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PROGRAM_MEMORY_ERROR), "Program memory error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 13:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.RAM_MEMORY_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.RAM_MEMORY_ERROR), "RAM error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 14:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.NV_MEMORY_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.NV_MEMORY_ERROR), "NV memory error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 15:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.WATCHDOG_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.WATCHDOG_ERROR), "Watchdog error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 16:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MEASUREMENT_SYSTEM_ERROR), "Measurement system error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 17:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.FIRMWARE_READY_FOR_ACTIVATION), "Firmware ready for activation", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 18:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.FIRMWARE_ACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.FIRMWARE_ACTIVATED), "Firmware activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 26:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COMMUNICATION_STARTED_ON_REMOTE_INTERFACE_LAN_WAN, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COMMUNICATION_STARTED_ON_REMOTE_INTERFACE_LAN_WAN), "Communication started on remote interface LAN/WAN", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 27:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ENDED_ON_REMOTE_INTERFACE_LAN_WAN, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COMMUNICATION_ENDED_ON_REMOTE_INTERFACE_LAN_WAN), "Communication ended on remote interface LAN/WAN", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 28:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COMMUNICATION_STARTED_ON_LOCAL_INTERFACE_WZ, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COMMUNICATION_STARTED_ON_LOCAL_INTERFACE_WZ), "Communication started on local interface WZ", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 29:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ENDED_ON_LOCAL_INTERFACE_WZ, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COMMUNICATION_ENDED_ON_LOCAL_INTERFACE_WZ), "Communication ended on local interface WZ", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 40:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.TERMINAL_OPENED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.TERMINAL_OPENED), "Terminal cover removed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 41:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.TERMINAL_COVER_CLOSED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.TERMINAL_COVER_CLOSED), "Terminal cover closed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 42:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.STRONG_DC_FIELD_DETECTED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.STRONG_DC_FIELD_DETECTED), "Strong DC field detected", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 43:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.NO_STRONG_DC_FIELD_ANYMORE), "No strong DC field anymore", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 44:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.METER_COVER_OPENED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.METER_COVER_OPENED), "Meter cover removed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 45:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.METER_COVER_CLOSED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.METER_COVER_CLOSED), "Meter cover closed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 46:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.N_TIMES_WRONG_PASSWORD, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.N_TIMES_WRONG_PASSWORD), "Association authentication failure", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 48:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.GLOBAL_KEY, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.GLOBAL_KEY), "Global key(s) changed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 49:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.N_TIMES_DECRYPT_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.N_TIMES_DECRYPT_FAIL), "Decryption or authentication failure", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 50:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.REPLAY_ATTACK, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.REPLAY_ATTACK), "Replay attack", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 51:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.FIRMWARE_VERIFICATION_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.FIRMWARE_VERIFICATION_FAIL), "FW verification/activation failed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 53:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.REINITIALIZATION_RNG, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.REINITIALIZATION_RNG), "Re-initialization of the RNG", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 54:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOCAL_INTERFACE_DEACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOCAL_INTERFACE_DEACTIVATED), "Local interface de-activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 55:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOCAL_INTERFACE_REACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOCAL_INTERFACE_REACTIVATED), "Local interface re-activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 59:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.DISCONNECTOR_READY_FOR_RECONN, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.DISCONNECTOR_READY_FOR_RECONN), "Disconnector ready for manual reconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 60:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MANUAL_DISCONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MANUAL_DISCONNECTION), "Manual disconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 61:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MANUAL_CONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MANUAL_CONNECTION), "Manual connection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 62:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.REMOTE_DISCONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.REMOTE_DISCONNECTION), "Remote disconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 63:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.REMOTE_CONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.REMOTE_CONNECTION), "Remote connection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 64:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOCAL_DISCONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOCAL_DISCONNECTION), "Local disconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 65:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LIMITER_THRESHOLD_EXCEEDED), "Limiter threshold exceeded", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 66:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LIMITER_THRESHOLD_OK, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LIMITER_THRESHOLD_OK), "Limiter threshold OK", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 67:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LIMITER_THRESHOLD_CHANGED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LIMITER_THRESHOLD_CHANGED), "Limiter threshold config changed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 68:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.DISCONNECT_RECONNECT_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.DISCONNECT_RECONNECT_FAIL), "Disconnect/Reconnect failure", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 69:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOCAL_RECONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOCAL_RECONNECTION), "Local reconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 95:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PASSIVE_ACTIVITY_CALENDAR_ACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PASSIVE_ACTIVITY_CALENDAR_ACTIVATED), "Disconnector control - passive activity calendar activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 96:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.ACTIVITY_CALENDAR_PROGRAMMED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.ACTIVITY_CALENDAR_PROGRAMMED), "Disconnector control - activity calendar programmed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 99:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.CONSUMER_MESSAGE_UPDATE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.CONSUMER_MESSAGE_UPDATE), "Consumer message update", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 200:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.CONSUMER_INTERFACE_DEACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.CONSUMER_INTERFACE_DEACTIVATED), "Consumer interface de-activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 201:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.CONSUMER_INTERFACE_REACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.CONSUMER_INTERFACE_REACTIVATED), "Consumer interface re-activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 203:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MODEM_REMOVED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MODEM_REMOVED), "Modem removed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 204:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PLC_G3_PSK_CHANGE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PLC_G3_PSK_CHANGE), "G3 PSK change", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 255:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.EVENT_LOG_CLEARED), "Event log cleared", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            default:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Unknown event code: " + eventId, SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
        }
        return event;
    }
}
