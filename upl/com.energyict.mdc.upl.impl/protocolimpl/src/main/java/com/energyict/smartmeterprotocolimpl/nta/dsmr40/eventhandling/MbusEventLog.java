package com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusLog;

import java.util.*;

/**
 * Extends the original DSMR2.3 MbusLog with additional events for DSMR4.0
 */
public class MbusEventLog extends MbusLog{

    private static final int EVENT_NEW_MBUS_DISCOVERED_1 = 105;
    private static final int EVENT_NEW_MBUS_DISCOVERED_2 = 115;
    private static final int EVENT_NEW_MBUS_DISCOVERED_3 = 125;
    private static final int EVENT_NEW_MBUS_DISCOVERED_4 = 135;

    public MbusEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch(eventId){
            case EVENT_NEW_MBUS_DISCOVERED_1 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 1"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_2 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 2"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_3 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 3"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_4 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 4"));}break;
            default: super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
