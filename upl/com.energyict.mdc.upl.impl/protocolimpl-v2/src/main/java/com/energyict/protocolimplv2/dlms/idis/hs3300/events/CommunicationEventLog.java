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
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.COMMUNICATION_TIMEOUT, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.COMMUNICATION_TIMEOUT), "No connection timeout", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 17:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MODEM_INITIALIZATION_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MODEM_INITIALIZATION_FAIL), "Modem initialization failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 18:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SIM_CARD_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SIM_CARD_FAIL), "SIM card failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 19:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SIM_CARD_OK, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SIM_CARD_OK), "SIM card OK", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 20:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.GSM_GPRS_REGISTRATION_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.GSM_GPRS_REGISTRATION_FAIL), "GSM registration failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 21:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.GSM_GPRS_REGISTRATION_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.GSM_GPRS_REGISTRATION_FAIL), "GPRS registration failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 22:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PDP_CONTEXT_ESTABLISHED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PDP_CONTEXT_ESTABLISHED), "PDP context established", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 23:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PDP_CONTEXT_DESTROYED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PDP_CONTEXT_DESTROYED), "PDP context destroyed", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 24:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PDP_CONTEXT_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PDP_CONTEXT_FAIL), "PDP context failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 25:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MODEM_SW_RESET, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MODEM_SW_RESET), "Modem SW reset", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 26:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MODEM_HW_RESET, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MODEM_HW_RESET), "Modem HW reset", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 27:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.GSM_CONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.GSM_CONNECTION), "GSM outgoing connection", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 28:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.GSM_CONNECTION, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.GSM_CONNECTION), "GSM incoming connection", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 29:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.GSM_HANG_UP, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.GSM_HANG_UP), "GSM hang-up", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 30:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.DIAGNOSTIC_FAILURE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.DIAGNOSTIC_FAILURE), "Diagnostic failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 31:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.USER_INITIALIZATION_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.USER_INITIALIZATION_FAIL), "User initialization failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 32:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SIGNAL_QUALITY_LOW, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SIGNAL_QUALITY_LOW), "Signal quality low", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 33:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.ANSWER_NUMBER_EXCEEDED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.ANSWER_NUMBER_EXCEEDED), "Auto answer number of calls exceeded", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 34:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.MODEM_FAILS_RESPONSE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.MODEM_FAILS_RESPONSE), "ping_response_not_received start", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 35:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.TCP_IP_CONNECTION_ESTABLISHMENT_FAILURE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.TCP_IP_CONNECTION_ESTABLISHMENT_FAILURE), "TCP/IP_connection_establishment_failure", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 36:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.ACK_NOT_RECEIVED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.ACK_NOT_RECEIVED), "Ack_not_received", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 37:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.SIGNAL_QUALITY_RESTORED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.SIGNAL_QUALITY_RESTORED), "Signal quality low end", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 38:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PING_RESPONSE_NOT_RECEIVED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PING_RESPONSE_NOT_RECEIVED), "ping_response_not_received end", COMMUNICATION_EVENT_LOG_ID, UNKNOWN_ID);
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
