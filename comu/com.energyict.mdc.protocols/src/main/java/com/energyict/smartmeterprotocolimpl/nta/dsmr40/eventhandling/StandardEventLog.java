package com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.EventsLog;

import java.util.Date;
import java.util.List;

/**
 * Extends the original DSMR2.3 EventsLog with additional events for DSMR4.0
 */
public class StandardEventLog extends EventsLog {

	protected static final int EVENT_TARIFF_SHIFT_TIME = 19;
	protected static final int EVENT_SELF_CHECK_AFTER_FIRMWARE = 20;

    public StandardEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch(eventId){
            case EVENT_TARIFF_SHIFT_TIME : {meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Tariff shift time (TOU)"));}break;
            case EVENT_SELF_CHECK_AFTER_FIRMWARE : {meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Successful selfcheck after firmwareupdate"));}break;
            default: super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
