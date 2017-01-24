package com.energyict.protocolimplv2.abnt.common.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class BcdEncodedField extends AbstractField<BcdEncodedField> {

    private int length;
    private String text;

    public BcdEncodedField() {
        this(1, "");
    }

    public BcdEncodedField(int length) {
        this(length, "");
    }

    public BcdEncodedField(String text) {
        this(1, text);
    }

    public BcdEncodedField(int length, String text) {
        this.length = length;
        this.text = text;
    }

    @Override
    public byte[] getBytes() {
        return getBCDFromHexString(text, length);
    }

    @Override
    public BcdEncodedField parse(byte[] rawData, int offset) throws ParsingException {
        this.text = getHexStringFromBCD(rawData, offset, getLength());
        return this;
    }

    @Override
    public int getLength() {
        return length;
    }

    public long getValue() throws ParsingException {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new ParsingException("Failed to parse value of BcdEncodedfield (" + text + ") as number: " + e.getMessage());
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}