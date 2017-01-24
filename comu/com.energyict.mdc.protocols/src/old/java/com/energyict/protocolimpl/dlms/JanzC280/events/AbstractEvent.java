package com.energyict.protocolimpl.dlms.JanzC280.events;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 13/12/11
 * Time: 11:10
**/

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
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
     */
    protected abstract void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId);

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
     * @throws java.io.IOException
     */
    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(1) & 0xFF; // To prevent negative values
            eventTimeStamp = null;
            if (isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))) {
                eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray())).getValue().getTime();
            }
            if (eventTimeStamp != null) {
                buildMeterEvent(meterEvents, eventTimeStamp, eventId);
            }
        }
        return meterEvents;
    }

    /**
     * Checks if the given {@link Object} is an {@link com.energyict.dlms.OctetString}
     *
     * @param element the object to check the type
     * @return true or false
     */
    protected boolean isOctetString(Object element) {
        return (element instanceof com.energyict.dlms.OctetString);
    }
}
