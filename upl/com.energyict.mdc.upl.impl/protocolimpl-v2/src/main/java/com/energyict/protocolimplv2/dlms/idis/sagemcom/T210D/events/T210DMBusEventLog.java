package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130MBusEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by cisac on 6/30/2016.
 */
public class T210DMBusEventLog extends AM130MBusEventLog {

    public T210DMBusEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 208:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "M-Bus device uninstalled channel 1"));
                break;
            case 209:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "M-Bus device uninstalled channel 2"));
                break;
            case 210:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "M-Bus device uninstalled channel 3"));
                break;
            case 211:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "M-Bus device uninstalled channel 4"));
                break;
            case 214:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "High frame counter channel 1"));
                break;
            case 215:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "High frame counter channel 2"));
                break;
            case 216:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "High frame counter channel 3"));
                break;
            case 217:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "High frame counter channel 4"));
                break;
            case 220:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Temporary Error M-Bus channel 1"));
                break;
            case 221:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Temporary Error M-Bus channel 2"));
                break;
            case 222:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Temporary Error M-Bus channel 3"));
                break;
            case 223:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Temporary Error M-Bus channel 4"));
                break;

            case 212:
            case 213:
            case 218:
            case 219:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
