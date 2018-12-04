package com.energyict.protocolimplv2.nta.dsmr40.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.MbusLog;

import java.util.Date;
import java.util.List;

/**
 * Extends the original DSMR2.3 MbusLog with additional events for DSMR4.0
 */
public class MbusEventLog extends MbusLog {

    private static final int EVENT_NEW_MBUS_DISCOVERED_1 = 105;
    private static final int EVENT_PERMANENT_ERROR_MBUS_1 = 106;
    private static final int EVENT_NEW_MBUS_DISCOVERED_2 = 115;
    private static final int EVENT_PERMANENT_ERROR_MBUS_2 = 116;
    private static final int EVENT_NEW_MBUS_DISCOVERED_3 = 125;
    private static final int EVENT_PERMANENT_ERROR_MBUS_3 = 126;
    private static final int EVENT_NEW_MBUS_DISCOVERED_4 = 135;
    private static final int EVENT_PERMANENT_ERROR_MBUS_4 = 136;

    public MbusEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }
    public MbusEventLog(DataContainer dc, int mBusChannel) {
        super(dc, mBusChannel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        //select only event ids that correspond to mbus channel
        //channel 1: 100 .. 109
        //channel 2: 110 .. 119
        //channel 3: 120 .. 129
        //channel 4: 130 .. 139
        int clonedEventId = eventId;
        //if outside range, consider it an unknown event
        if (eventId != 255 && eventId < (90 + mBusChannel * 10) && eventId > (99 + mBusChannel * 10))
            clonedEventId = 0;
        switch(clonedEventId){
            case EVENT_NEW_MBUS_DISCOVERED_1 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 1"));}break;
            case EVENT_PERMANENT_ERROR_MBUS_1 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Permanent error on Mbus channel 1"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_2 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 2"));}break;
            case EVENT_PERMANENT_ERROR_MBUS_2 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Permanent error on Mbus channel 2"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_3 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 3"));}break;
            case EVENT_PERMANENT_ERROR_MBUS_3 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Permanent error on Mbus channel 3"));}break;
            case EVENT_NEW_MBUS_DISCOVERED_4 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "A new M-Bus Device has been detected on channel 4"));}break;
            case EVENT_PERMANENT_ERROR_MBUS_4 : {meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Permanent error on Mbus channel 4"));}break;
            default: super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
