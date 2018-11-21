package com.energyict.protocolimplv2.nta.dsmr23.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.mdc.upl.ProtocolException;

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
    protected final DataContainer dcEvents;

    protected final AXDRDateTimeDeviationType deviationType;

    /**
     * @param dc the DataContainer, containing all the eventData
     * @param deviationType the interpretation type of the DataTime
     */
    public AbstractEvent(DataContainer dc, final AXDRDateTimeDeviationType deviationType) {
        this.dcEvents = dc;
        this.deviationType = deviationType;
    }

    /**
     * @param dc the DataContainer, containing all the eventData
     */
    public AbstractEvent(DataContainer dc) {
        this(dc, AXDRDateTimeDeviationType.Negative);
    }

    /**
     * @return the a MeterEvent List
     * @throws java.io.IOException
     */
    public List<MeterEvent> getMeterEvents() throws ProtocolException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp = null;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(1) & 0xFF; // To prevent negative values
            if (isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))) {
                eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray()), this.deviationType).getValue().getTime();
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
