package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MagneticInfluenceLog extends AbstractEvent {

    private static final int MAGNETIC_INFLUENCE_START = 0x01;
    private static final int MAGNETIC_INFLUENCE_STOP = 0x00;

    public MagneticInfluenceLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case MAGNETIC_INFLUENCE_START:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.STRONG_DC_FIELD_DETECTED, eventId, "Influence of magnetic field start"));
                break;
            case MAGNETIC_INFLUENCE_STOP:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, eventId, "Influence of magnetic field stop"));
                break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}