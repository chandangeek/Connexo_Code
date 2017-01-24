package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class LogBookEventNr extends AbstractField<LogBookEventNr> {

    public static final int LENGTH = 1;

    private int nr;

    public LogBookEventNr() {
        this.nr = 0;
    }

    public LogBookEventNr(int nr) {
        this.nr = nr;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(nr, LENGTH);
    }

    @Override
    public LogBookEventNr parse(byte[] rawData, int offset) throws ParsingException {
        nr = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getNr() {
        return nr;
    }
}