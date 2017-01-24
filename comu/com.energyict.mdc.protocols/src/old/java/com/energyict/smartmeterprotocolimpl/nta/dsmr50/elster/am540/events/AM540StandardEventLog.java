package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.StandardEventLog;

import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/06/2014 - 13:25
 */
public class AM540StandardEventLog extends StandardEventLog {

    private static final int EVENT_PARAMETERS_CHANGED = 47;
    private static final int EVENT_PHASE_SEQUENCE_REVERSAL = 88;
    private static final int EVENT_MISSING_NEUTRAL = 89;

    public AM540StandardEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch (eventId) {
            case EVENT_CLOCK_INVALID: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CLOCK_INVALID, eventId, "Clock invalid, power reserve may be exhausted"));
            }
            break;
            case EVENT_TOU_ACTIVATED: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.TOU_ACTIVATED, eventId, "TOU activated"));
            }
            break;
            case EVENT_PROGRAM_MEMORY_ERROR: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.PROGRAM_MEMORY_ERROR, eventId, "Program memory error"));
            }
            break;
            case EVENT_RAM_ERROR: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.RAM_MEMORY_ERROR, eventId, "RAM error"));
            }
            break;
            case EVENT_NV_MEMORY_ERROR: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.NV_MEMORY_ERROR, eventId, "NV memory error"));
            }
            break;
            case EVENT_WATCHDOG_ERROR: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.WATCHDOG_ERROR, eventId, "Watchdog error"));
            }
            break;
            case EVENT_MEASUREMENT_SYSTEM_ERROR: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Measurement system error"));
            }
            break;
            case EVENT_FIRMWARE_READY_ACTIVATION: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, eventId, "Firmware ready for activation"));
            }
            break;
            case EVENT_FIRMWARE_ACTIVATED: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.FIRMWARE_ACTIVATED, eventId, "Firmware activated"));
            }
            break;
            case EVENT_TARIFF_SHIFT_TIME: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Passive TOU programmed"));
            }
            break;
            case EVENT_PARAMETERS_CHANGED: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "One or more parameters changed"));
            }
            break;
            case EVENT_PHASE_SEQUENCE_REVERSAL: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Phase sequence reversal"));
            }
            break;
            case EVENT_MISSING_NEUTRAL: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Missing neutral"));
            }
            break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}