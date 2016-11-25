package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.protocolimpl.dlms.idis.events.MBusControlLog2;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AM130MBusControlLog2 extends MBusControlLog2 {

    public AM130MBusControlLog2(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 175:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local disconnection M-Bus channel 2"));
                break;
            case 176:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local connection M-Bus channel 2"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}