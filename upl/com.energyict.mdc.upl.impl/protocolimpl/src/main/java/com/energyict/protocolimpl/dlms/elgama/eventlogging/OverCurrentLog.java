package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

public class OverCurrentLog extends AbstractEvent {

    private static final int OVER_CURRENT_L1 = 0x01;
    private static final int OVER_CURRENT_L2 = 0x02;
    private static final int OVER_CURRENT_L3 = 0x04;
    private static final int OVER_CURRENT_NEUTRAL = 0x08;
    private static final int OVER_CURRENT_END_IN_ALL_PHASES = 0x00;


    public OverCurrentLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {

        previousSize = meterEvents.size();
        this.meterEvents = meterEvents;

        if ((eventId & OVER_CURRENT_L1) != 0) {
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, OVER_CURRENT_L1, "Over-current in phase L1"));
        }
        if ((eventId & OVER_CURRENT_L2) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, OVER_CURRENT_L2, "Over-current in phase L2"));
        }
        if ((eventId & OVER_CURRENT_L3) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, OVER_CURRENT_L3, "Over-current in phase L3"));
        }
        if ((eventId & OVER_CURRENT_NEUTRAL) != 0) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, OVER_CURRENT_NEUTRAL, "Over-current in neutral"));
        }
        if (eventId == OVER_CURRENT_END_IN_ALL_PHASES) {
            eventTimeStamp = fixStamp(eventTimeStamp);
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, OVER_CURRENT_END_IN_ALL_PHASES, "End of over-current events in all phases"));
        }
        if (!anEventWasAdded()) {
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode:" + eventId));
        }
    }
}