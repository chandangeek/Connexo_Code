package com.energyict.protocolimplv2.abnt.common.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class BcdEncodedField extends AbstractField<BcdEncodedField> {

    private int length;
    private int number;

    public BcdEncodedField() {
        this.length = 1;
    }

    public BcdEncodedField(int length) {
        this.length = length;
    }

    public BcdEncodedField(int length, int number) {
        this.length = length;
        this.number = number;
    }

    @Override
    public byte[] getBytes() {
        return getBCDFromInt(number, length);
    }

    @Override
    public BcdEncodedField parse(byte[] rawData, int offset) throws ParsingException {
        String text = getHexStringFromBCD(rawData, offset, getLength());
        try {
            Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ParsingException("Failed to extract a valid number from the BCD encoded value", e);
        }
        return this;
    }

    @Override
    public int getLength() {
        return length;
    }

    public int getNumber() {
        return number;
    }
}