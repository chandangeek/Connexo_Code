package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.AbstractEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AM130MBusControlLog5 extends AbstractEvent {

    public AM130MBusControlLog5(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 220:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "Manual disconnection M-Bus channel 5"));
                break;
            case 221:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "Manual connection M-Bus channel 5"));
                break;
            case 222:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "Remote disconnection M-Bus channel 5"));
                break;
            case 223:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "Remote connection M-Bus channel 5"));
                break;
            case 224:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm M-Bus channel 5"));
                break;
            case 225:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local disconnection M-Bus channel 5"));
                break;
            case 226:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local connection M-Bus channel 5"));
                break;
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "MBus control 5 event log cleared"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}