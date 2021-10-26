package com.energyict.protocolimplv2.dlms.idis.iskra.am550.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.AbstractEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Dmitry Borisov on 26/10/2021.
 */
public class Am550CommunicationDetailEventLog extends AbstractEvent {

    Am550CommunicationDetailEventLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 26:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION_START, eventId, "Communication started on remote interface I3 or I3.1"));
                break;
            case 27:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION, eventId, "Communication ended on remote interface I3 or I3.1"));
                break;
            case 28:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION_START, eventId, "Communication started on local interface IE-M"));
                break;
            case 29:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION, eventId, "Communication ended on local interface IE-M"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}
