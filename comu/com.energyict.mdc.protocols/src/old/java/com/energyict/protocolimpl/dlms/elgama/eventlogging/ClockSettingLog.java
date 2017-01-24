package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ClockSettingLog extends AbstractEvent {

    private static final int CLOCK_SETTING_START = 0x01;
    private static final int CLOCK_SETTING_END = 0x00;

    public ClockSettingLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case CLOCK_SETTING_START:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK_BEFORE, eventId, "Clock setting start (old timestamp)"));
                break;
            case CLOCK_SETTING_END:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK_AFTER, eventId, "Clock setting start (new timestamp)"));
                break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}