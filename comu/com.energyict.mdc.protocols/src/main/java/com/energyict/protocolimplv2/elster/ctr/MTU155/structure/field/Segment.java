package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the Segment field in a CTR Structure Object - used in firmware upgrade process
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
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