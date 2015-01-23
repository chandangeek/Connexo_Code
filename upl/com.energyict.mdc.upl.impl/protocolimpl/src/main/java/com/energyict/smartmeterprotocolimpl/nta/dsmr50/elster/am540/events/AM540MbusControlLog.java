package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusControlLog;

import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 23/01/2015 - 11:59
 */
public class AM540MbusControlLog extends MbusControlLog {

    private static final int EVENT_LOCAL_DISCONNECTION_MBUS1 = 165;
    private static final int EVENT_LOCAL_CONNECTION_MBUS1 = 166;

    private static final int EVENT_LOCAL_DISCONNECTION_MBUS2 = 175;
    private static final int EVENT_LOCAL_CONNECTION_MBUS2 = 176;

    private static final int EVENT_LOCAL_DISCONNECTION_MBUS3 = 185;
    private static final int EVENT_LOCAL_CONNECTION_MBUS3 = 186;

    private static final int EVENT_LOCAL_DISCONNECTION_MBUS4 = 195;
    private static final int EVENT_LOCAL_CONNECTION_MBUS4 = 196;

    public AM540MbusControlLog(DataContainer dc) {
        super(dc);
    }

    public AM540MbusControlLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch (eventId) {
            case EVENT_LOCAL_DISCONNECTION_MBUS1: {
                meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been locally disconnected (MBus1)"));
            }
            break;
            case EVENT_LOCAL_CONNECTION_MBUS1: {
                meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been locally connected (MBus1)"));
            }
            break;
            case EVENT_LOCAL_DISCONNECTION_MBUS2: {
                meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been locally disconnected (MBus2)"));
            }
            break;
            case EVENT_LOCAL_CONNECTION_MBUS2: {
                meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been locally connected (MBus2)"));
            }
            break;
            case EVENT_LOCAL_DISCONNECTION_MBUS3: {
                meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been locally disconnected (MBus3)"));
            }
            break;
            case EVENT_LOCAL_CONNECTION_MBUS3: {
                meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been locally connected (MBus3)"));
            }
            break;
            case EVENT_LOCAL_DISCONNECTION_MBUS4: {
                meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been locally disconnected (MBus4)"));
            }
            break;
            case EVENT_LOCAL_CONNECTION_MBUS4: {
                meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been locally connected (MBus4)"));
            }
            break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
