package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MBusControlLog4 extends AbstractEvent {

    public MBusControlLog4(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 190:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "Manual disconnection M-Bus channel 4"));
                break;
            case 191:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "Manual connection M-Bus channel 4"));
                break;
            case 192:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "Remote disconnection M-Bus channel 4"));
                break;
            case 193:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "Remote connection M-Bus channel 4"));
                break;
            case 194:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm M-Bus channel 4"));
                break;
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "MBus control 4 event log cleared"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}