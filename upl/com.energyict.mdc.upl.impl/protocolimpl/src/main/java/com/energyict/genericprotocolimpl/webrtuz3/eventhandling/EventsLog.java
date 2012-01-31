package com.energyict.genericprotocolimpl.webrtuz3.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.*;

public class EventsLog {

    private DataContainer dcEvents;

    /**
     * Create a new eventsLog from a given DataContainer
     *
     * @param dc
     */
    public EventsLog(DataContainer dc) {
        this.dcEvents = dc;
    }

    /**
     * @return
     * @throws IOException
     */
    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        DataStructure dataStructureRoot = this.dcEvents.getRoot();
        for (int i = 0; i < dataStructureRoot.getNrOfElements(); i++) {
            Date eventTimeStamp = null;
            int eventId = (int) dataStructureRoot.getStructure(i).getValue(1) & 0xFF; // To prevent negative values
            if (isOctetString(dataStructureRoot.getStructure(i).getElement(0))) {
                eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray())).getValue().getTime();
            }
            if (eventTimeStamp != null) {
                MeterEvent event;
                if (ExtraEvents.contains(eventId)) {
                    event = ExtraEvents.getExtraEvent(eventTimeStamp, eventId);
                } else {
                    event = new MeterEvent(eventTimeStamp, eventId, eventId);
                }
                // Create a new meter Event using the toString method of the previous event
                // to work around the missing resources problem in EiServer ([JIRA] EISERVER-583) 
                meterEvents.add(new MeterEvent(eventTimeStamp, eventId, eventId, event.toString()));
            }
        }
        return meterEvents;
    }

    /**
     * @param element
     * @return
     */
    private boolean isOctetString(Object element) {
        return (element instanceof com.energyict.dlms.OctetString) ? true : false;
    }


}
