package com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.EventsLog;

import java.util.*;

/**
 * Extends the original DSMR2.3 EventsLog with additional events for DSMR4.0
 */
public class StandardEventLog extends EventsLog{

	private static final int EVENT_TARIFF_SHIFT_TIME = 19;
	private static final int EVENT_SELF_CHECK_AFTER_FIRMWARE = 20;

    public StandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch(eventId){
            case EVENT_TARIFF_SHIFT_TIME : {meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Change of tariff shift times has occurred."));}break;
            case EVENT_SELF_CHECK_AFTER_FIRMWARE : {meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Indicates that the first selfcheck after a firmwareupdate was performed successfully."));}break; 
            default: super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
