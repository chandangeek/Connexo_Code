package com.energyict.protocolimplv2.nta.dsmr40.eventhandling;


import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.AbstractEvent;

import java.util.Date;
import java.util.List;

public class VoltageQualityEventLog extends AbstractEvent {
    private static final int EVENT_SHORT_VOLTAGE_SAG_L1 = 77;
    private static final int EVENT_SHORT_VOLTAGE_SAG_L2 = 78;
    private static final int EVENT_SHORT_VOLTAGE_SAG_L3 = 79;

    /**
     * @param dc            the DataContainer, containing all the eventData
     * @param deviationType the interpretation type of the DataTime
     */
    public VoltageQualityEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }
    public VoltageQualityEventLog(DataContainer dc) {
        super(dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case EVENT_SHORT_VOLTAGE_SAG_L1: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Short voltage sag L1"));
            }
            break;
            case EVENT_SHORT_VOLTAGE_SAG_L2: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Short voltage sag L2"));
            }
            break;
            case EVENT_SHORT_VOLTAGE_SAG_L3: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Short voltage sag L3"));
            }
            break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}
