package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.protocolimpl.dlms.idis.events.MBusControlLog4;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AM130MBusControlLog4 extends MBusControlLog4 {

    public AM130MBusControlLog4(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 195:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local disconnection M-Bus channel 4"));
                break;
            case 196:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local connection M-Bus channel 4"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}