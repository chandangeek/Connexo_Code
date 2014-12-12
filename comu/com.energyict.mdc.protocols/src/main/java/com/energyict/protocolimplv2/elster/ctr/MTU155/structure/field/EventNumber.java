package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the Trigger Event Number field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class EventNumber extends AbstractField<EventNumber> {

    private int eventNumber;

    public EventNumber() {
        this(0);
    }

    public EventNumber(int coda) {
        this.eventNumber = coda;
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(eventNumber, getLength());
    }

    public EventNumber parse(byte[] rawData, int offset) throws CTRParsingException {
        this.eventNumber = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(int eventNumber) {
        this.eventNumber = eventNumber;
    }
}
