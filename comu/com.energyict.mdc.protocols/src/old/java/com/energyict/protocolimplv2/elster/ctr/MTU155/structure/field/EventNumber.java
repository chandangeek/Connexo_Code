/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

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
