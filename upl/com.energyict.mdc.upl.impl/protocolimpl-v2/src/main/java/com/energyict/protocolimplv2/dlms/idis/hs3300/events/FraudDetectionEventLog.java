package com.energyict.protocolimplv2.dlms.idis.hs3300.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Date;

public class FraudDetectionEventLog {

    private static int FRAUD_DETECTION_EVENT_LOG = 1;
    private static int UNKNOWN_ID = 0;

    public static MeterProtocolEvent buildMeterEvent(Date eventTimeStamp, int eventId) {
        MeterProtocolEvent event;
        switch (eventId) {
            case 40:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.TERMINAL_OPENED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.TERMINAL_OPENED), "Terminal cover removed", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 41:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.TERMINAL_COVER_CLOSED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.TERMINAL_COVER_CLOSED), "Terminal cover closed", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 42:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.STRONG_DC_FIELD_DETECTED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.STRONG_DC_FIELD_DETECTED), "Strong DC field detected", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 43:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.NO_STRONG_DC_FIELD_ANYMORE), "No strong DC field anymore", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 44:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.METER_COVER_OPENED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.METER_COVER_OPENED), "Meter cover removed", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 45:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.METER_COVER_CLOSED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.METER_COVER_CLOSED), "Meter cover closed", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 46:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.N_TIMES_WRONG_PASSWORD, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.N_TIMES_WRONG_PASSWORD), "Association authentication failure", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 49:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.N_TIMES_DECRYPT_FAIL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.N_TIMES_DECRYPT_FAIL), "Decryption or authentication failure", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 50:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.REPLAY_ATTACK, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.REPLAY_ATTACK), "Replay attack", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 53:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.REINITIALIZATION_RNG, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.REINITIALIZATION_RNG), "Re-initialization of the RNG", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 54:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOCAL_INTERFACE_DEACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOCAL_INTERFACE_DEACTIVATED), "Local interface de-activated", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 55:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.LOCAL_INTERFACE_REACTIVATED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.LOCAL_INTERFACE_REACTIVATED), "Local interface re-activated", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 91:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.CURRENT_REVERSAL, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.CURRENT_REVERSAL), "Current reversal", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            case 255:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.EVENT_LOG_CLEARED), "Event log cleared", FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
                break;
            default:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Unknown event code: " + eventId, FRAUD_DETECTION_EVENT_LOG, UNKNOWN_ID);
        }
        return event;
    }
}
