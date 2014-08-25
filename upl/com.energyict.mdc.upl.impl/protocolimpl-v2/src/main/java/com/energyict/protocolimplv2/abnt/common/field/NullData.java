package com.energyict.protocolimplv2.abnt.common.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;

/**
 * A NullData field contains only empty (0x00) bytes.
 *
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class NullData extends AbstractField<NullData> {

    private final int length;

    public NullData(int length) {
        this.length = length;
    }

    @Override
    public byte[] getBytes() {
        return new byte[length];
    }

    @Override
    public NullData parse(byte[] rawData, int offset) throws ParsingException {
        return this;
    }

    @Override
    public int getLength() {
        return length;
    }
}