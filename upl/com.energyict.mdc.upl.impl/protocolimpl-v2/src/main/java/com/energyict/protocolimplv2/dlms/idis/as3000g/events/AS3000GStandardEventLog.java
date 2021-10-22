package com.energyict.protocolimplv2.dlms.idis.as3000g.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AS3000GStandardEventLog extends StandardEventLog {

    public AS3000GStandardEventLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 52:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNEXPECTED_CONSUMPTION, eventId, "Unexpected consumption"));
                break;
            case 200:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CT_VT_RATIO_CHANGED, eventId, "Indicates that the CT or VT Ratio is changed"));
                break;
            case 203:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.START_CERTIFICATION_MODE, eventId, "Indicates that the certification mode is started"));
                break;
            case 204:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.END_CERTIFICATION_MODE, eventId, "Indicates that the certification mode is ended"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}