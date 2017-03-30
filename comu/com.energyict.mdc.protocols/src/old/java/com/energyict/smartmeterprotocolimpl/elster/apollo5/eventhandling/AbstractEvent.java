/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public abstract class AbstractEvent {

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    protected abstract int getGroupId();

    /**
     * Container containing raw events
     */
    protected final DataContainer dcEvents;

    /**
     * The used {@link java.util.TimeZone}
     */
    protected final TimeZone timeZone;

    /**
     * Constructor
     *
     * @param dc       the dataContainer containing all the raw events
     * @param timeZone the timezone to use for parsing eventTimes
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

    /**
     * Create an EventString from the given {@link ApolloEvents}
     *
     * @param apolloEvent the given ApolloEvents
     * @return a readable event String
     */
    protected String createEventString(ApolloEvents apolloEvent) {
        StringBuilder sb = new StringBuilder();
        sb.append("Group [ ");
        sb.append(apolloEvent.getGroupDescription());
        sb.append(" ] - Description [ ");
        sb.append(apolloEvent.getDescription());
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * Build a MeterEvent based on the protocolEventCode
     *
     * @param meterEvents    the list to fill up with the MeterEvent
     * @param eventTimeStamp the timeStamp for the new Event
     * @param eventId        the eventCode returned from the device
     */
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        ApolloEvents ae = ApolloEvents.find(eventId, getGroupId());
        meterEvents.add(new MeterEvent(eventTimeStamp, ae.getEIServerCode(), eventId, createEventString(ae)));
    }

}
