package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.*;

/**
 * The Abstract Event class contains common functionality for the NTA event profiles
 *
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
     * @param meterEvents
     * @param eventTimeStamp
     * @param eventId
     */
    protected abstract void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId);

    /**
     * Container containing raw events
     */
    protected DataContainer dcEvents;

    /**
     * Constructor
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
                eventTimeStamp = new AXDRDateTime(new OctetString(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray()), AXDRDateTimeDeviationType.Positive).getValue().getTime();
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
     * @param element
     *          the object to check the type
     *
     * @return true or false
     */
    protected boolean isOctetString(Object element) {
        return (element instanceof com.energyict.dlms.OctetString) ? true : false;
    }
}
