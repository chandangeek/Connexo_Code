package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class ReadingSelector extends AbstractField<ReadingSelector> {

    public static final int LENGTH = 1;

    private int readingSelector;

    public ReadingSelector() {
        this.readingSelector = 0;
    }

    public ReadingSelector(int readingSelector) {
        this.readingSelector = readingSelector;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(readingSelector, LENGTH);
    }

    @Override
    public ReadingSelector parse(byte[] rawData, int offset) throws ParsingException {
        this.readingSelector = (getIntFromBytes(rawData, offset, LENGTH) & 0x01);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getReadingSelector() {
        return readingSelector;
    }

    public void setReadingSelector(int readingSelector) {
        this.readingSelector = readingSelector;
    }
}