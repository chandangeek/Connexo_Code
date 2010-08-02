package com.energyict.genericprotocolimpl.nta.elster.logs;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * <p>
 * Copyrights EnergyICT
 * Date: 4-jun-2010
 * Time: 10:32:00
 * </p>
 */
public class EventsLog extends com.energyict.genericprotocolimpl.nta.eventhandling.EventsLog {

    public EventsLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        System.out.println("Printing DataContainer for Events");
        this.dcEvents.printDataContainer();
        Date eventTimeStamp = null;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(1) & 0xFF; // To prevent negative values
            if (isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))) {
//                eventTimeStamp = dcEvents.getRoot().getStructure(i).getOctetString(0).toCalendar(timeZone).getTime();
                eventTimeStamp = dcEvents.getRoot().getStructure(i).getOctetString(0).toCalendar(timeZone).getTime();
//                eventTimeStamp = new AXDRDateTime(new OctetString(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray())).getValue().getTime();
            }

            if (eventTimeStamp != null) {
                buildMeterEvent(meterEvents, eventTimeStamp, eventId);
            }
        }
        return meterEvents;
    }
}
