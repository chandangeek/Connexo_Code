package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

public class PowerQualityEventLog extends AbstractEvent {

    public PowerQualityEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 76:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNDERVOLTAGE_L1, eventId, "Undervoltage L1"));
                break;
            case 77:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNDERVOLTAGE_L2, eventId, "Undervoltage L2"));
                break;
            case 78:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNDERVOLTAGE_L3, eventId, "Undervoltage L3"));
                break;
            case 79:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERVOLTAGE_L1, eventId, "Overvoltage L1"));
                break;
            case 80:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERVOLTAGE_L2, eventId, "Overvoltage L2"));
                break;
            case 81:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OVERVOLTAGE_L3, eventId, "Overvoltage L3"));
                break;
            case 82:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MISSINGVOLTAGE_L1, eventId, "Missing voltage L1"));
                break;
            case 83:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MISSINGVOLTAGE_L2, eventId, "Missing voltage L2"));
                break;
            case 84:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MISSINGVOLTAGE_L3, eventId, "Missing voltage L3"));
                break;
            case 85:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NORMALVOLTAGE_L1, eventId, "Voltage L1 normal"));
                break;
            case 86:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NORMALVOLTAGE_L2, eventId, "Voltage L2 normal"));
                break;
            case 87:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NORMALVOLTAGE_L3, eventId, "Voltage L3 normal"));
                break;
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Power quality event log cleared"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}