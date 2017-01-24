package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MeterCoverOpenedLog extends AbstractEvent {

    private static final int METER_COVER_OPEN_START = 0x01;
    private static final int METER_COVER_OPEN_END = 0x00;

    public MeterCoverOpenedLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case METER_COVER_OPEN_START:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COVER_OPENED, eventId, "Opening of meter cover event start"));
                break;
            case METER_COVER_OPEN_END:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_COVER_CLOSED, eventId, "Opening of meter cover event end"));
                break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}