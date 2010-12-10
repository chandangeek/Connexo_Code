package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

public class PowerFailureLog extends AbstractEvent {

    private static final int EVENT_POWER_STARTUP = 0x00;
    private static final int EVENT_POWER_OUTAGE = 0x01;

    public PowerFailureLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case EVENT_POWER_OUTAGE:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.POWERDOWN, eventId, "Fraud detection event log profile cleared."));
            break;
            case EVENT_POWER_STARTUP:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.POWERUP, eventId, "The terminal cover has been removed"));
            break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}