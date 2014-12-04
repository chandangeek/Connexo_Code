package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 26/08/2014 - 12:00
 */
public class LoadProfileBlockArgument extends AbstractField<LoadProfileBlockArgument> {

    private static final int LENGTH = 1;

    int blockCount;

    public LoadProfileBlockArgument() {
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(blockCount, LENGTH);
    }

    @Override
    public LoadProfileBlockArgument parse(byte[] rawData, int offset) throws ParsingException {
        blockCount = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getLoadProfileBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }
}