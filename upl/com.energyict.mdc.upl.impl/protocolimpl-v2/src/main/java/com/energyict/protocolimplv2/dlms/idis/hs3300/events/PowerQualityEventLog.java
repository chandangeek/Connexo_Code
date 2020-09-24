package com.energyict.protocolimplv2.dlms.idis.hs3300.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Date;

public class PowerQualityEventLog {

    private static int POWER_QUALITY_EVENT_LOG_ID = 4;
    private static int UNKNOWN_ID = 0;

    public static MeterProtocolEvent buildMeterEvent(Date eventTimeStamp, int eventId) {
        MeterProtocolEvent event;
        switch (eventId) {
            case 76:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Undervoltage L1", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 77:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Undervoltage L2", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 78:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Undervoltage L3", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 79:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Overvoltage L1", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 80:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Overvoltage L2", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 81:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Overvoltage L3", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 82:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Missing voltage L1", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 83:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Missing voltage L2", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 84:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Missing voltage L3", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 85:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Voltage L1 normal", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 86:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Voltage L2 normal", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 87:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Voltage L3 normal", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 90:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.PHASE_ASYMMETRY, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.PHASE_ASYMMETRY), "Phase Asymmetry", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 92:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.BADVOLTAGE_L1, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.BADVOLTAGE_L1), "Bad Voltage Quality L1", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 93:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.BADVOLTAGE_L2, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.BADVOLTAGE_L2), "Bad Voltage Quality L2", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 94:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.BADVOLTAGE_L3, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.BADVOLTAGE_L3), "Bad Voltage Quality L3", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            case 255:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.EVENT_LOG_CLEARED), "Event log cleared", POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
                break;
            default:
                event = new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventId,
                        EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), "Unknown event code: " + eventId, POWER_QUALITY_EVENT_LOG_ID, UNKNOWN_ID);
        }
        return event;
    }
}
