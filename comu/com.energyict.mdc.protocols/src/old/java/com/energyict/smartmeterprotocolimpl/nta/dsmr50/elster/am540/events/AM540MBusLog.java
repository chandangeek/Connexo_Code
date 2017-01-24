package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusLog;

import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 13/06/2014 - 15:57
 */
public class AM540MBusLog extends MbusLog {

    private static final int EVENT_NEW_MBUS_DEVICE_DISCOVERED1 = 105;
    private static final int EVENT_PERMANENT_ERROR_MBUS1 = 106;
    private static final int EVENT_TEMPORARY_ERROR_MBUS1 = 107;

    private static final int EVENT_NEW_MBUS_DEVICE_DISCOVERED2 = 115;
    private static final int EVENT_PERMANENT_ERROR_MBUS2 = 116;
    private static final int EVENT_TEMPORARY_ERROR_MBUS2 = 117;

    private static final int EVENT_NEW_MBUS_DEVICE_DISCOVERED3 = 125;
    private static final int EVENT_PERMANENT_ERROR_MBUS3 = 126;
    private static final int EVENT_TEMPORARY_ERROR_MBUS3 = 127;

    private static final int EVENT_NEW_MBUS_DEVICE_DISCOVERED4 = 135;
    private static final int EVENT_PERMANENT_ERROR_MBUS4 = 136;
    private static final int EVENT_TEMPORARY_ERROR_MBUS4 = 137;


    public AM540MBusLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch (eventId) {
            case EVENT_NEW_MBUS_DEVICE_DISCOVERED1: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "New M-Bus device discovered channel 1"));
            }
            break;
            case EVENT_PERMANENT_ERROR_MBUS1: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.FATAL_ERROR, eventId, "Permanent error from M-Bus device channel 1"));
            }
            break;
            case EVENT_TEMPORARY_ERROR_MBUS1: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Non-permanent error from M-Bus device channel 1"));
            }
            break;
            case EVENT_NEW_MBUS_DEVICE_DISCOVERED2: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "New M-Bus device discovered channel 2"));
            }
            break;
            case EVENT_PERMANENT_ERROR_MBUS2: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.FATAL_ERROR, eventId, "Permanent error from M-Bus device channel 2"));
            }
            break;
            case EVENT_TEMPORARY_ERROR_MBUS2: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Non-permanent error from M-Bus device channel 2"));
            }
            break;
            case EVENT_NEW_MBUS_DEVICE_DISCOVERED3: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "New M-Bus device discovered channel 3"));
            }
            break;
            case EVENT_PERMANENT_ERROR_MBUS3: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.FATAL_ERROR, eventId, "Permanent error from M-Bus device channel 3"));
            }
            break;
            case EVENT_TEMPORARY_ERROR_MBUS3: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Non-permanent error from M-Bus device channel 3"));
            }
            break;
            case EVENT_NEW_MBUS_DEVICE_DISCOVERED4: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "New M-Bus device discovered channel 4"));
            }
            break;
            case EVENT_PERMANENT_ERROR_MBUS4: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.FATAL_ERROR, eventId, "Permanent error from M-Bus device channel 4"));
            }
            break;
            case EVENT_TEMPORARY_ERROR_MBUS4: {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Non-permanent error from M-Bus device channel 4"));
            }
            break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}