package com.energyict.protocolimplv2.dlms.idis.aec.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AECStandardEventLog extends StandardEventLog {

    public AECStandardEventLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 20:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EXTERNAL_ALERT, eventId, "External alert detected"));
                break;
            case 21:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATION_PARAMETER_CHANGED, eventId, "Parameter modification"));
                break;
            case 52:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNEXPECTED_CONSUMPTION, eventId, "Unexpected consumption"));
                break;
            case 83:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NORMALVOLTAGE_L1, eventId, "Voltage L1 normal"));
                break;
            case 84:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NORMALVOLTAGE_L2, eventId, "Voltage L2 normal"));
                break;
            case 85:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NORMALVOLTAGE_L3, eventId, "Voltage L3 normal"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
