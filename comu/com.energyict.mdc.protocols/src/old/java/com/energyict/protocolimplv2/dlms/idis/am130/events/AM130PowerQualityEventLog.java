package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.protocolimpl.dlms.idis.events.PowerQualityEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AM130PowerQualityEventLog extends PowerQualityEventLog {

    public AM130PowerQualityEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 90:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Phase Asymmetry"));
                break;
            case 92:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Bad Voltage Quality L1"));
                break;
            case 93:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Bad Voltage Quality L2"));
                break;
            case 94:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Bad Voltage Quality L3"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}