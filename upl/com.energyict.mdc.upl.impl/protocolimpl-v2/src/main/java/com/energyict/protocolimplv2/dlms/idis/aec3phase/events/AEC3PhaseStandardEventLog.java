package com.energyict.protocolimplv2.dlms.idis.aec3phase.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.idis.aec.events.AECStandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AEC3PhaseStandardEventLog extends AECStandardEventLog {
    public AEC3PhaseStandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
