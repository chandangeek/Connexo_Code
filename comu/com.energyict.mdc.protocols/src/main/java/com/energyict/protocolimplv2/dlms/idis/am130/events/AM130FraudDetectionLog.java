package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.protocolimpl.dlms.idis.events.FraudDetectionLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AM130FraudDetectionLog extends FraudDetectionLog {

    public AM130FraudDetectionLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 91:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REVERSE_RUN, eventId, "Current Reversal"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}