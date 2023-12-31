package com.energyict.protocolimplv2.abnt.common.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;

import java.util.Arrays;

/**
 * @author sva
 * @since 23/05/2014 - 10:19
 */
public abstract class AbstractField<T extends Field> implements Field<T> {

    private static final int FLOAT_BYTE_LENGTH = 4;

    protected int getIntFromBytes(byte[] rawData, int offset, int length) {
       return ProtocolTools.getIntFromBytes(rawData, offset, length);
    }

    protected int getIntFromBytesLE(byte[] rawData, int offset, int length) {
        return ProtocolTools.getIntFromBytesLE(rawData, offset, length);
    }

    protected float getFloatFromBytes(byte[] rawData, int offset) {
        int intBits = getIntFromBytes(rawData, offset, FLOAT_BYTE_LENGTH);
        return Float.intBitsToFloat(intBits);
    }

    protected float getFloatFromBytesLE(byte[] rawData, int offset) {
        int intBits = getIntFromBytesLE(rawData, offset, FLOAT_BYTE_LENGTH);
        return Float.intBitsToFloat(intBits);
    }

    protected String getHexStringFromBCD(byte[] rawData, int offset, int length) throws ParsingException {
        return ProtocolTools.getHexStringFromBytes(ProtocolTools.getSubArray(rawData, offset, offset + length), "");
    }

    protected int getIntFromBCD(byte[] rawData, int offset, int length) throws ParsingException {
        try {
            return Integer.parseInt(getHexStringFromBCD(rawData, offset, length));
        } catch (NumberFormatException e) {
            throw new ParsingException(e);
        }
    }

    protected byte[] getBytesFromInt(int value, int length) {
        return ProtocolTools.getBytesFromInt(value, length);
    }

    protected byte[] getBytesFromIntLE(int value, int length) {
        return ProtocolTools.getBytesFromIntLE(value, length);
    }

    protected byte[] getBCDFromHexString(String hexString, int length) {
        return ProtocolTools.getBCDFromHexString(hexString, length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Field)) {
            return false;
        }

        Field that = (Field) o;
        return Arrays.equals(this.getBytes(), that.getBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }
}