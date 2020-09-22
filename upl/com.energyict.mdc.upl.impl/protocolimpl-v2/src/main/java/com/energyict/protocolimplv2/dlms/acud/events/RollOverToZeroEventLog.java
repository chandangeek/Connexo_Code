package com.energyict.protocolimplv2.dlms.acud.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class RollOverToZeroEventLog extends AbstractEvent {

    public RollOverToZeroEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, DataStructure evStructure) {
        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ROLL_OVER_TO_ZERO, eventId, "Roll over to zero"));
    }
}