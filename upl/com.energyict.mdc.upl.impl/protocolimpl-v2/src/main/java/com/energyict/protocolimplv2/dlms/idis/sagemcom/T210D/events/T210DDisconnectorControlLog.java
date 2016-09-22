package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.DisconnectorControlLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by cisac on 6/30/2016.
 */
public class T210DDisconnectorControlLog extends com.energyict.protocolimpl.dlms.idis.events.DisconnectorControlLog {

    public T210DDisconnectorControlLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 224:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Remote disconnection relay 1"));
                break;
            case 225:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Remote disconnection relay 2"));
                break;
            case 226:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Remote disconnection relay 3"));
                break;
            case 227:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Remote reconnection relay 1"));
                break;
            case 228:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Remote reconnection relay 2"));
                break;
            case 229:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Remote reconnection relay 3"));
                break;
            case 230:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local disconnection relay 1"));
                break;
            case 231:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local disconnection relay 2"));
                break;
            case 232:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local disconnection relay 3"));
                break;
            case 233:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local reconnection relay 1"));
                break;
            case 234:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local reconnection relay 2"));
                break;
            case 235:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local reconnection relay 3"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
