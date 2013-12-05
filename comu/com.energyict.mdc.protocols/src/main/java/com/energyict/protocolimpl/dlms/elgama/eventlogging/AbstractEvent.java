package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * The Abstract Event class contains common functionality for the NTA event profiles
 * <p/>
 * <p>
 * Copyrights EnergyICT
 * Date: 4-jun-2010
 * Time: 10:33:53
 * </p>
 */
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
    protected static final int ONE_SECOND = 1000;
    protected int previousSize = 0;
    protected List<MeterEvent> meterEvents;

    protected boolean anEventWasAdded() {
        return !(previousSize == meterEvents.size());
    }

    /**
     * The G3B meter can put multiple events on the same timestamp.
     * In order to make them all show up in EiServer, their timestamp needs to be altered by e.g. 1 second.
     *
     * @param eventTimeStamp
     * @return
     */
    protected Date fixStamp(Date eventTimeStamp) {
        if (anEventWasAdded()) {
            eventTimeStamp.setTime(eventTimeStamp.getTime() + ONE_SECOND);
        }
        return eventTimeStamp;
    }

    /**
     * Constructor
     *
     * @param dc
     */
    public AbstractEvent(DataContainer dc, TimeZone timeZone) {
        this.dcEvents = dc;
        this.timeZone = timeZone;
    }

    /**
     * @return the a MeterEvent List
     * @throws java.io.IOException
     */
    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp = null;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(1) & 0xFF; // To prevent negative values
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
        return (element instanceof com.energyict.dlms.OctetString) ? true : false;
    }
}
