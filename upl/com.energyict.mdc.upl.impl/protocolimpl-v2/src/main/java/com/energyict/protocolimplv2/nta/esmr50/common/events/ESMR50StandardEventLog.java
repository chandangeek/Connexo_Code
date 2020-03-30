package com.energyict.protocolimplv2.nta.esmr50.common.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.StandardEventLog;

import java.util.Date;
import java.util.List;

/**
 * Extends the original DSMR4.0 EventsLog with additional events for ESMR5.0
 */
public class ESMR50StandardEventLog extends StandardEventLog {

    protected static final int EVENT_SUCCESSFULL_SELFCHECK_AFTER_FIRMWARE_UPDATE = 20;
    protected static final int EVENT_SUCCESSFULL_FIRWARE_UPGRADE_LTE_MODEM = 21;
    protected static final int EVENT_TOO_HIGH_CONSUMPTION_OR_PRODUCTION = 22;
    protected static final int EVENT_INDEX_VALUE_DECREASE_OR_RESET = 23;
    protected static final int EVENT_MISTMATCH_BETWEEN_TOTAL_AND_TARIFF_REGISTERS = 24;


    public ESMR50StandardEventLog(DataContainer dc) {
        super(dc);
    }

    public ESMR50StandardEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId)
    {
        switch( eventId )
        {
            case EVENT_SUCCESSFULL_SELFCHECK_AFTER_FIRMWARE_UPDATE:
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Succesfull selfcheck after Firmware update"));
                break;
            case EVENT_SUCCESSFULL_FIRWARE_UPGRADE_LTE_MODEM:
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Succesfull FW upgrade LTE modem"));
                break;
            case EVENT_TOO_HIGH_CONSUMPTION_OR_PRODUCTION:
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.TOO_HIGH_CONSUMPTION_OR_PRODUCTION, eventId, "Too high consumption or production of energy"));
                break;
            case EVENT_INDEX_VALUE_DECREASE_OR_RESET:
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.INDEX_VALUE_DECREASE_OR_RESET, eventId, "Decreasing index values or reset of index values"));
                break;
            case EVENT_MISTMATCH_BETWEEN_TOTAL_AND_TARIFF_REGISTERS:
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.MISMATCH_BETWEEN_TOTAL_AND_TARIFF_REGISTERS, eventId, "Mismatch between total registers and tariff registers"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
