package com.energyict.protocolimplv2.dlms.idis.aec.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AECStandardEventLog extends StandardEventLog {

    public AECStandardEventLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 20:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.INPUT_SIGNAL_DETECTION, eventId, "Input signal detected"));
                break;
            case 21:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATION_PARAMETER_CHANGED, eventId, "Parameter modification"));
                break;
            case 22:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SECURITY_KEYS_UPDATED, eventId, "Key modification"));
                break;
            case 23:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPDATE_VERIFY_FAILURE, eventId, "Firmware upgrade verification failure"));
                break;
            case 25:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOADPROFILE_CLEARED, eventId, "Load profile cleared"));
                break;
            case 26:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Event log cleared"));
                break;
            case 30:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DISCONNECTOR_READY_FOR_RECONN, eventId, "Disconnector ready for manual reconnection"));
                break;
            case 31:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION, eventId, "Manual disconnection"));
                break;
            case 32:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION, eventId, "Manual connection"));
                break;
            case 33:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION, eventId, "Remote disconnection"));
                break;
            case 34:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION, eventId, "Remote connection"));
                break;
            case 35:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_DISCONNECTION, eventId, "Local disconnection"));
                break;
            case 36:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "Limiter threshold exceeded"));
                break;
            case 37:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_OK, eventId, "Limiter threshold ok"));
                break;
            case 38:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_CHANGED, eventId, "Limiter threshold changed"));
                break;
            case 39:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DISCONNECT_RECONNECT_FAIL, eventId, "Disconnect/Reconnect failure"));
                break;
            case 40:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_RECONNECTION, eventId, "Local reconnection"));
                break;
            case 41:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_1_EXCEEDED, eventId, "Supervision monitor 1 threshold exceeded"));
                break;
            case 42:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_1_OK, eventId, "Supervision monitor 1 threshold ok"));
                break;
            case 43:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_2_EXCEEDED, eventId, "Supervision monitor 2 threshold exceeded"));
                break;
            case 44:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_2_OK, eventId, "Supervision monitor 2 threshold ok"));
                break;
            case 45:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_3_EXCEEDED, eventId, "Supervision monitor 3 threshold exceeded"));
                break;
            case 46:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_3_OK, eventId, "Supervision monitor 3 threshold ok"));
                break;
            case 50:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TERMINAL_OPENED, eventId, "Terminal cover removed"));
                break;
            case 51:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TERMINAL_COVER_CLOSED, eventId, "Terminal cover closed"));
                break;
            case 52:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.STRONG_DC_FIELD_DETECTED, eventId, "Strong DC field detected"));
                break;
            case 53:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, eventId, "No strong DC field anymore"));
                break;
            case 54:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_COVER_OPENED, eventId, "Meter cover removed"));
                break;
            case 55:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_COVER_CLOSED, eventId, "Meter cover closed"));
                break;
            case 56:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.N_TIMES_WRONG_PASSWORD, eventId, "Association authentication failure"));
                break;
            case 57:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DECRYPTION_OR_AUTHENTICATION_FAILURE, eventId, "Decryption or authentication failure"));
                break;
            case 58:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLAY_ATTACK, eventId, "Replay attack"));
                break;
            case 59:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CURRENT_REVERSAL, eventId, "Current reversal"));
                break;
            case 60:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Neutral fault"));
                break;
            case 61:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Neutral fault end"));
                break;
            case 62:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERCURRENT_L1, eventId, "Overcurrent L1"));
                break;
            case 63:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERCURRENT_L2, eventId, "Overcurrent L2"));
                break;
            case 64:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERCURRENT_L3, eventId, "Overcurrent L3"));
                break;
            case 65:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERCURRENT_STOPPED_L1, eventId, "Overcurrent stopped L1"));
                break;
            case 66:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERCURRENT_STOPPED_L2, eventId, "Overcurrent stopped L2"));
                break;
            case 67:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERCURRENT_STOPPED_L3, eventId, "Overcurrent stopped L3"));
                break;
            case 68:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FACTOR_DEVIATION, eventId, "L1 factor deviation"));
                break;
            case 69:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FACTOR_DEVIATION, eventId, "L2 factor deviation"));
                break;
            case 70:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FACTOR_DEVIATION, eventId, "L3 factor deviation"));
                break;
            case 71:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FACTOR_DEVIATION_END, eventId, "L1 factor deviation end"));
                break;
            case 72:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FACTOR_DEVIATION_END, eventId, "L2 factor deviation end"));
                break;
            case 73:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FACTOR_DEVIATION_END, eventId, "L3 factor deviation end"));
                break;
            case 74:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNDERVOLTAGE_L1, eventId, "Undervoltage L1"));
                break;
            case 75:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNDERVOLTAGE_L2, eventId, "Undervoltage L2"));
                break;
            case 76:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNDERVOLTAGE_L3, eventId, "Undervoltage L3"));
                break;
            case 77:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERVOLTAGE_L1, eventId, "Overvoltage L1"));
                break;
            case 78:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERVOLTAGE_L2, eventId, "Overvoltage L2"));
                break;
            case 79:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERVOLTAGE_L3, eventId, "Overvoltage L3"));
                break;
            case 80:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MISSINGVOLTAGE_L1, eventId, "Missing voltage L1"));
                break;
            case 81:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MISSINGVOLTAGE_L2, eventId, "Missing voltage L2"));
                break;
            case 82:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MISSINGVOLTAGE_L3, eventId, "Missing voltage L3"));
                break;
            case 83:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NORMALVOLTAGE_L1, eventId, "Voltage L1 normal"));
                break;
            case 84:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NORMALVOLTAGE_L2, eventId, "Voltage L2 normal"));
                break;
            case 85:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NORMALVOLTAGE_L3, eventId, "Voltage L3 normal"));
                break;
            case 86:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_REVERSAL, eventId, "Phase sequence reversal"));
                break;
            case 87:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MISSING_NEUTRAL, eventId, "Missing neutral"));
                break;
            case 88:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_ASYMMETRY, eventId, "Phase Asymmetry"));
                break;
            case 89:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNDERVOLTAGE_L1, eventId, "Critical undervoltage L1"));
                break;
            case 90:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNDERVOLTAGE_L2, eventId, "Critical undervoltage L2"));
                break;
            case 91:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNDERVOLTAGE_L3, eventId, "Critical undervoltage L3"));
                break;
            case 92:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERVOLTAGE_L1, eventId, "Critical overvoltage L1"));
                break;
            case 93:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERVOLTAGE_L2, eventId, "Critical overvoltage L2"));
                break;
            case 94:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERVOLTAGE_L3, eventId, "Critical overvoltage L3"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
