package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.MBusEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AM130MBusEventLog extends MBusEventLog {

    public AM130MBusEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 105:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "New M-Bus device installed channel 1"));
                break;
            case 106:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Permanent Error M-Bus channel 1"));
                break;
            case 115:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "New M-Bus device installed channel 2"));
                break;
            case 116:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Permanent Error M-Bus channel 2"));
                break;
            case 125:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "New M-Bus device installed channel 3"));
                break;
            case 126:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Permanent Error M-Bus channel 3"));
                break;
            case 135:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "New M-Bus device installed channel 4"));
                break;
            case 136:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Permanent Error M-Bus channel 4"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}