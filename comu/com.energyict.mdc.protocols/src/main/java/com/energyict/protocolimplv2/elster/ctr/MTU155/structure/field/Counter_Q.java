/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class Counter_Q extends AbstractField<Counter_Q> {

    private int counter_Q;

    public Counter_Q() {
        this(0);
    }

    public Counter_Q(int counter_Q) {
        this.counter_Q = counter_Q;
    }

    public byte[] getBytes() {
        return getBytesFromInt(counter_Q, getLength());
    }

    public Counter_Q parse(byte[] rawData, int offset) throws CTRParsingException {
        this.counter_Q = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getLength() {
        return 1;
    }

    public int getCounter_Q() {
        return counter_Q;
    }

    public void setCounter_Q(int counter_Q) {
        this.counter_Q = counter_Q;
    }
}
