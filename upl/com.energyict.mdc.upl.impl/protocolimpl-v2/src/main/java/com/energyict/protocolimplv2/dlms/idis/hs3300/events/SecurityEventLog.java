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
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Power down", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 2:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Power up", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 4:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Clock adjusted (old date/time)", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 5:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Clock adjusted (new date/time)", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 10:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Error register cleared", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 11:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Alarm register cleared", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 12:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Program memory error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 13:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "RAM error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 14:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "NV memory error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 15:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Watchdog error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 16:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Measurement system error", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 17:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Firmware ready for activation", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 18:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Firmware activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 26:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Communication started on remote interface LAN/WAN", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 27:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Communication ended on remote interface LAN/WAN", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 28:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Communication started on local interface WZ", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 29:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Communication ended on local interface WZ", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
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
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COVER_OPENED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COVER_OPENED), "Meter cover removed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
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
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Global key(s) changed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 49:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Decryption or authentication failure", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 50:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Replay attack", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 51:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "FW verification/activation failed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 53:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Re-initialization of the RNG", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 54:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Local interface de-activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 55:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Local interface re-activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 59:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Disconnector ready for manual reconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 60:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Manual disconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 61:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Manual connection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 62:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Remote disconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 63:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Remote connection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 64:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Local disconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 65:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Limiter threshold exceeded", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 66:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Limiter threshold OK", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 67:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Limiter threshold config changed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 68:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Disconnect/Reconnect failure", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 69:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Local reconnection", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 95:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Disconnector control - passive activity calendar activated", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 96:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Disconnector control - activity calendar programmed", SECURITY_EVENT_LOG_ID, UNKNOWN_ID);
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
