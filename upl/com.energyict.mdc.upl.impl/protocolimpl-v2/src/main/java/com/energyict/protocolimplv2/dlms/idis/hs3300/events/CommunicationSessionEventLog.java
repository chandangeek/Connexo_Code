package com.energyict.protocolimplv2.dlms.idis.hs3300.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Date;

public class CommunicationSessionEventLog {

    private static int COMMUNICATION_SESSION_EVENT_LOG_ID = 6;
    private static int UNKNOWN_ID = 0;

    public static MeterProtocolEvent buildMeterEvent(Date eventTimeStamp, int eventId) {
        MeterProtocolEvent event;
        switch (eventId) {
            case 26:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COMMUNICATION_STARTED_ON_REMOTE_INTERFACE_LAN_WAN, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COMMUNICATION_STARTED_ON_REMOTE_INTERFACE_LAN_WAN), "Communication started on remote interface LAN/WAN", COMMUNICATION_SESSION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 27:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ENDED_ON_REMOTE_INTERFACE_LAN_WAN, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COMMUNICATION_ENDED_ON_REMOTE_INTERFACE_LAN_WAN), "Communication ended on remote interface LAN/WAN", COMMUNICATION_SESSION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 28:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COMMUNICATION_STARTED_ON_LOCAL_INTERFACE_WZ, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COMMUNICATION_STARTED_ON_LOCAL_INTERFACE_WZ), "Communication started on local interface WZ", COMMUNICATION_SESSION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 29:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ENDED_ON_LOCAL_INTERFACE_WZ, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COMMUNICATION_ENDED_ON_LOCAL_INTERFACE_WZ), "Communication ended on local interface WZ", COMMUNICATION_SESSION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 255:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.EVENT_LOG_CLEARED), "Event log cleared", COMMUNICATION_SESSION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            default:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Unknown event code: " + eventId, COMMUNICATION_SESSION_EVENT_LOG_ID, UNKNOWN_ID);
        }
        return event;
    }
}
