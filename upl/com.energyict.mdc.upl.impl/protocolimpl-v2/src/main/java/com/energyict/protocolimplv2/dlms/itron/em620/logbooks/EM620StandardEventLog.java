package com.energyict.protocolimplv2.dlms.itron.em620.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EM620StandardEventLog extends StandardEventLog {

    public EM620StandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {

            case 21:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DEVICE_RESET, eventId, "Global meter reset"));
                break;
            case 22:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_STATUS_DISABLED, eventId, "Battery removed"));
                break;
            case 23:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_STATUS_ENABLED, eventId, "Battery removed end"));
                break;
            case 24:
                // TODO meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent., eventId, "Display set mode active"));
                break;
            case 25:
                // TODO meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent., eventId, "Display set mode deactive"));
                break;
            case 26:
                // TODO meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent., eventId, "Display set mode success config"));
                break;
            case 27:
                // TODO meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent., eventId, "Display set mode reject config"));
                break;
            case 49:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UPDATED_MASTERKEY, eventId, "Master key changed"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
