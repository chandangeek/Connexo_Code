package com.energyict.protocolimplv2.dlms.idis.AS3000G.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130FraudDetectionLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AS3000GFraudDetectionLog extends AM130FraudDetectionLog {

    public AS3000GFraudDetectionLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(timeZone, dc, isMirrorConnection);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 241:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMS_HUB_PRESENT, eventId, "Communication Module Present"));
                break;
            case 242:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COMMS_HUB_REMOVED, eventId, "Communication Module removed"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}