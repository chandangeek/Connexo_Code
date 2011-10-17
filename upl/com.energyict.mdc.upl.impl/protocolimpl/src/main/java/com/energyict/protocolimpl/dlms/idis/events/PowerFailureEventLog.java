package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

public class PowerFailureEventLog extends AbstractEvent {

    public PowerFailureEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VOLTAGE_SAG, eventId, "Long power failure in a phase, duration: " + eventId + " seconds"));
    }
}