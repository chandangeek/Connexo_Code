/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class Segment extends AbstractField<Segment> {

    private int segment;
    private static final int LENGTH = 2;

    public Segment(int segment) {
        this.segment = segment;
    }

    public Segment() {
    }

    public int getSegment() {
        return segment;
    }

    public void setSegment(int segment) {
        this.segment = segment;
    }

    public byte[] getBytes() {
        return getBytesFromInt(getSegment(), LENGTH);
    }

    public Segment parse(byte[] rawData, int offset) throws CTRParsingException {
        setSegment(getIntFromBytes(rawData, offset, LENGTH));
        return this;
    }

    public int getLength() {
        return LENGTH;
    }
}