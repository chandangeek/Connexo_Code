package com.energyict.protocolimplv2.dlms.idis.aec3phase.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AEC3PhaseStandardEventLog extends StandardEventLog {
    public AEC3PhaseStandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 49:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DECRYPTION_OR_AUTHENTICATION_FAILURE, eventId, "Decryption or authentication failure"));
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
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RELAY_DISCONNECTED, eventId, "E-relay 1 off"));
                break;
            case 245:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RELAY_CONNECTED, eventId, "E-relay 1 on"));
                break;
            case 246:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RELAY_DISCONNECTED, eventId, "E-relay 2 off"));
                break;
            case 247:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RELAY_CONNECTED, eventId, "E-relay 2 on"));
                break;
            case 248:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_COMM_START, eventId, "RS-485 access"));
                break;
            case 249:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_COMM_START, eventId, "Local communication start"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
