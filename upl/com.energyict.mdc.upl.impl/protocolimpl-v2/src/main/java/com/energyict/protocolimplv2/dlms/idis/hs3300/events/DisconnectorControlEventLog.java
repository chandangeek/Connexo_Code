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
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.DISCONNECTOR_READY_FOR_RECONN, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.DISCONNECTOR_READY_FOR_RECONN), "Disconnector ready for manual reconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 60:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MANUAL_DISCONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MANUAL_DISCONNECTION), "Manual disconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 61:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MANUAL_CONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MANUAL_CONNECTION), "Manual connection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 62:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.REMOTE_DISCONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.REMOTE_DISCONNECTION), "Remote disconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 63:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.REMOTE_CONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.REMOTE_CONNECTION), "Remote connection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 64:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOCAL_DISCONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOCAL_DISCONNECTION), "Local disconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 65:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LIMITER_THRESHOLD_EXCEEDED), "Limiter threshold exceeded", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 66:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LIMITER_THRESHOLD_OK, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LIMITER_THRESHOLD_OK), "Limiter threshold OK", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 67:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LIMITER_THRESHOLD_CHANGED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LIMITER_THRESHOLD_CHANGED), "Limiter threshold config changed", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 68:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.DISCONNECT_RECONNECT_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.DISCONNECT_RECONNECT_FAIL), "Disconnect/Reconnect failure", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 69:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOCAL_RECONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOCAL_RECONNECTION), "Local reconnection", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 70:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SUPERVISION_1_EXCEEDED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SUPERVISION_1_EXCEEDED), "Supervision monitor 1 threshold exceeded", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 71:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SUPERVISION_1_OK, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SUPERVISION_1_OK), "Supervision monitor 1 threshold OK", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 72:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SUPERVISION_2_EXCEEDED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SUPERVISION_2_EXCEEDED), "Supervision monitor 2 threshold exceeded", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 73:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SUPERVISION_2_OK, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SUPERVISION_2_OK), "Supervision monitor 2 threshold OK", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 74:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SUPERVISION_3_EXCEEDED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SUPERVISION_3_EXCEEDED), "Supervision monitor 3 threshold exceeded", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 75:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SUPERVISION_3_OK, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SUPERVISION_3_OK), "Supervision monitor 3 threshold OK", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 95:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PASSIVE_ACTIVITY_CALENDAR_ACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PASSIVE_ACTIVITY_CALENDAR_ACTIVATED), "Disconnector control - passive activity calendar activated", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 96:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.ACTIVITY_CALENDAR_PROGRAMMED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.ACTIVITY_CALENDAR_PROGRAMMED), "Disconnector control - activity calendar programmed", DISCONNECTOR_CONTROL_EVENT_LOG_ID, UNKNOWN_ID);
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
