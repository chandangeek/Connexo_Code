package com.energyict.protocolimplv2.dlms.idis.hs3300.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Date;

public class CommunicationEventLog {

    private static int COMMUNICATION_EVENT_LOG_ID = 5;
    private static int UNKNOWN_ID = 0;

    public static MeterProtocolEvent buildMeterEvent(Date eventTimeStamp, int eventId) {
        MeterProtocolEvent event;
        switch (eventId) {
            case 16:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "No connection timeout", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 17:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Modem initialization failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 18:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "SIM card failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 19:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "SIM card OK", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 20:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "GSM registration failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 21:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "GPRS registration failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 22:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "PDP context established", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 23:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "PDP context destroyed", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 24:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "PDP context failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 25:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Modem SW reset", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 26:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Modem HW reset", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 27:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "GSM outgoing connection", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 28:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "GSM incoming connection", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 29:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "GSM hang-up", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 30:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Diagnostic failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 31:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "User initialization failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 32:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Signal quality low", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 33:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Auto answer number of calls exceeded", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 34:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "ping_response_not_received start", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 35:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "TCP/IP_connection_establishment_failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 36:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Ack_not_received", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 37:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Signal quality low end", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 38:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "ping_response_not_received end", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 128:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - No connection timeout", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 129:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - Modem initialization failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 130:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - PAN registration success", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 131:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - PAN disconnect - KICK", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 132:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - PAN registration failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 133:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - ICMP RA received", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 134:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - ICMP RS failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 135:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - ICMP echo request received", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 136:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - Modem SW reset", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 137:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "G3 - Modem HW reset", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 255:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.EVENT_LOG_CLEARED), "Event log cleared", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            default:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Unknown event code: " + eventId, COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
        }
        return event;
    }
}
