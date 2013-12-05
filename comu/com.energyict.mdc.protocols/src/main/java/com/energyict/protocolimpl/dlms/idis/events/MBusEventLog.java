package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MBusEventLog extends AbstractEvent {

    public MBusEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 100:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication error M-Bus channel 1"));
                break;
            case 101:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication ok M-Bus channel 1"));
                break;
            case 102:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Replace Battery M-Bus channel 1"));
                break;
            case 103:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt M-Bus channel 1"));
                break;
            case 104:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock adjusted M-Bus channel 1"));
                break;
            case 110:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication error M-Bus channel 2"));
                break;
            case 111:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication ok M-Bus channel 2"));
                break;
            case 112:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Replace Battery M-Bus channel 2"));
                break;
            case 113:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt M-Bus channel 2"));
                break;
            case 114:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock adjusted M-Bus channel 2"));
                break;
            case 120:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication error M-Bus channel 3"));
                break;
            case 121:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication ok M-Bus channel 3"));
                break;
            case 122:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Replace Battery M-Bus channel 3"));
                break;
            case 123:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt M-Bus channel 3"));
                break;
            case 124:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock adjusted M-Bus channel 3"));
                break;
            case 130:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication error M-Bus channel 4"));
                break;
            case 131:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication ok M-Bus channel 4"));
                break;
            case 132:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Replace Battery M-Bus channel 4"));
                break;
            case 133:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt M-Bus channel 4"));
                break;
            case 134:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock adjusted M-Bus channel 4"));
                break;
            case 254:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOADPROFILE_CLEARED, eventId, "MBus load profile cleared"));
                break;
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Mbus event log cleared"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}