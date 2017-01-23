package com.energyict.protocolimplv2.abnt.common.field;

import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class FloatField extends AbstractField<FloatField> {

    private static final int FLOAT_BYTE_LENGTH = 4;

    private final Unit unit;

    private float value;

    public FloatField(Unit unit) {
        this.unit = unit;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(Float.floatToIntBits(value), FLOAT_BYTE_LENGTH);
    }

    @Override
    public FloatField parse(byte[] rawData, int offset) throws ParsingException {
        value = getFloatFromBytesLE(rawData, offset);
        return this;
    }

    @Override
    public int getLength() {
        return FLOAT_BYTE_LENGTH;
    }

    public float getValue() {
        return value;
    }

    public Unit getUnit() {
        return unit;
    }
}