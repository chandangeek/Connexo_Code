package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class PaddingData extends AbstractField<PaddingData> {

    private byte[] paddingBytes;
    private final int length;

    public PaddingData(int length) {
        this.length = length;
        this.paddingBytes = new byte[length];
    }

    public PaddingData(byte[] paddingBytes) {
        this.paddingBytes = paddingBytes;
        this.length = paddingBytes.length;
    }

    @Override
    public byte[] getBytes() {
        return paddingBytes;
    }

    @Override
    public PaddingData parse(byte[] rawData, int offset) throws ParsingException {
        paddingBytes = ProtocolTools.getSubArray(rawData, offset, offset + getLength());
        return this;
    }

    @Override
    public int getLength() {
        return length;
    }
}