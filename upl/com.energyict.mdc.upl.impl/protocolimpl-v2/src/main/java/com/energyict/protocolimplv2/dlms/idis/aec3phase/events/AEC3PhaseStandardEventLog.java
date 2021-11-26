package com.energyict.protocolimplv2.dlms.idis.aec3phase.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.idis.aec.events.AECStandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AEC3PhaseStandardEventLog extends AECStandardEventLog {
    public AEC3PhaseStandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 47:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATION_PARAMETER_CHANGED, eventId, "One or more parameters changed"));
                break;
            case 48:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SECURITY_KEYS_UPDATED, eventId, "Key modification"));
                break;
            case 49:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DECRYPTION_OR_AUTHENTICATION_FAILURE, eventId, "Decryption or authentication failure"));
                break;
            case 51:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPDATE_VERIFY_FAILURE, eventId, "Firmware upgrade verification failure"));
                break;
            case 88:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_REVERSAL, eventId, "Phase sequence reversal"));
                break;
            case 89:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MISSING_NEUTRAL, eventId, "Missing neutral"));
                break;
            case 236:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.HARDWARE_ERROR, eventId, "Hardware error"));
                break;
            case 242:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TARIFF_SCHEME_CHANGED, eventId, "Tariffication scheme changed"));
                break;
            case 243:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SEASON_CHANGE, eventId, "Season changed"));
                break;
            case 244:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RELAY_DISCONNECTED, eventId, "E-RELAY 1 OFF"));
                break;
            case 245:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RELAY_CONNECTED, eventId, "E-RELAY 1 ON"));
                break;
            case 246:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RELAY_DISCONNECTED, eventId, "E-RELAY 2 OFF"));
                break;
            case 247:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RELAY_CONNECTED, eventId, "E-RELAY 2 ON"));
                break;
            case 248:
                //meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent., eventId, "E_RS485_ACCESS"));
                break;
            case 249:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_COMM_START, eventId, "Local communication start"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
