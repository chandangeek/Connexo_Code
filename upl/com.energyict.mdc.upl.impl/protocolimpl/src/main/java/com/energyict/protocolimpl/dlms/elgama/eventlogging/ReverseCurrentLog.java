package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

public class ReverseCurrentLog extends AbstractEvent {

    private static final int REVERSE_CURRENT_STARTED = 0x01;
    private static final int REVERSE_CURRENT_ENDED= 0x00;

    public ReverseCurrentLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case REVERSE_CURRENT_STARTED:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Reverse current flow start"));
            break;
            case REVERSE_CURRENT_ENDED:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Reverse current flow end"));
            break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}