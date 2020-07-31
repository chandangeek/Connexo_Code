package com.energyict.protocolimplv2.dlms.acud.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public abstract class AbstractEvent {

    protected TimeZone timeZone;

    /**
     * Build a list of MeterEvents
     *
     * @param meterEvents
     * @param eventTimeStamp
     * @param eventId
     * @param evStructure
     */
    protected abstract void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, DataStructure evStructure);

    /**
     * Container containing raw events
     */
    protected DataContainer dcEvents;
    protected List<MeterEvent> meterEvents;

    public AbstractEvent(DataContainer dc, TimeZone timeZone) {
        this.dcEvents = dc;
        this.timeZone = timeZone;
    }

    /**
     * @return the MeterEvent List
     */
    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp;
        for (int i = 0; i <= (size - 1); i++) {
            DataStructure evStructure = dcEvents.getRoot().getStructure(i);
            int eventId = (int) evStructure.getValue(1) & 0xFF; // To prevent negative values
            if (evStructure.getElement(0) instanceof OctetString) {
                eventTimeStamp = evStructure.getOctetString(0).toDate(timeZone);
                buildMeterEvent(meterEvents, eventTimeStamp, eventId, evStructure);
            }
        }
        return meterEvents;
    }
}
