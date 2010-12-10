package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

public class FraudDetectionLog extends AbstractEvent {

    // Fraud detection log
    private static final int EVENT_METER_COVER_OPENED = 0x01;
    private static final int EVENT_METER_COVER_CLOSED = 0x00;

    public FraudDetectionLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {

        switch (eventId) {
            case EVENT_METER_COVER_OPENED:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COVER_OPENED, eventId, "The meter cover has been opened"));
            break;
            case EVENT_METER_COVER_CLOSED:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "The meter cover has been closed"));
            break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }

    }
}
