package com.energyict.protocolimplv2.abnt.common.frame.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class BlockCount extends AbstractField<BlockCount> {

    public static final int LENGTH = 2;

    private int blockCount;
    private boolean lastBlock;
    private BcdEncodedField encodedField;

    public BlockCount() {
        this.encodedField = new BcdEncodedField(LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return encodedField.getBytes();
    }

    @Override
    public BlockCount parse(byte[] rawData, int offset) throws ParsingException {
        this.encodedField.parse(rawData, offset);
        this.blockCount = Integer.parseInt(encodedField.getText()) & 0x7FFF;
        this.lastBlock = encodedField.getText().startsWith("1");
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public boolean isLastBlock() {
        return lastBlock;
    }
}