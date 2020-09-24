package com.energyict.protocolimplv2.dlms.idis.hs3300.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Date;

public class DisconnectorControlEventLog {

    private static int DISCONNECTOR_CONTROL_EVENT_LOG_ID = 2;
    private static int UNKNOWN_ID = 0;

    public static MeterProtocolEvent buildMeterEvent(Date eventTimeStamp, int eventId) {
        MeterProtocolEvent event;
        switch (eventId) {
            case 59:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Disconnector ready for manual reconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 60:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Manual disconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 61:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Manual connection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 62:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Remote disconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 63:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Remote connection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 64:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Local disconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 65:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Limiter threshold exceeded", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 66:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Limiter threshold OK", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 67:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Limiter threshold config changed", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 68:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Disconnect/Reconnect failure", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 69:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Local reconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 70:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Supervision monitor 1 threshold exceeded", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 71:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Supervision monitor 1 threshold OK", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 72:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Supervision monitor 2 threshold exceeded", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 73:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Supervision monitor 2 threshold OK", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 74:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Supervision monitor 3 threshold exceeded", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 75:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Supervision monitor 3 threshold OK", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 95:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Disconnector control - passive activity calendar activated", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 96:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Disconnector control - activity calendar programmed", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 255:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.EVENT_LOG_CLEARED), "Event log cleared", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            default:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Unknown event code: " + eventId, DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
        }
        return event;
    }
}
