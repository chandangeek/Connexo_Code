package com.energyict.protocolimplv2.elster.garnet.common.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author sva
 * @since 23/05/2014 - 10:19
 */
public abstract class AbstractField<T extends Field> implements Field<T> {

    private static final int BINAIR_RADIX = 2;
    private static final int NR_OF_BITS_PER_BYTE = 8;

    protected int getIntFromBytes(byte[] rawData, int offset, int length) {
        return ProtocolTools.getIntFromBytes(rawData, offset, length);
    }

    protected int getIntFromBytesLE(byte[] rawData, int offset, int length) {
        return ProtocolTools.getIntFromBytesLE(rawData, offset, length);
    }

    protected String getHexStringFromBCD(byte[] rawData, int offset, int length) throws ParsingException {
        return ProtocolTools.getHexStringFromBytes(ProtocolTools.getSubArray(rawData, offset, offset + length), "");
    }

    protected byte[] getBytesFromInt(int value, int length) {
        return ProtocolTools.getBytesFromInt(value, length);
    }

    protected byte[] getBytesFromIntLE(int value, int length) {
      return ProtocolTools.getBytesFromIntLE(value, length);
    }

    protected byte[] getBCDFromHexString(String hexString, int length) {
        return ProtocolTools.getBytesFromHexString(hexString, length);
    }

    protected String getBitStringFromByteArray(byte[] bytes) {
        // Create a BigInteger using the byte array
        BigInteger bi = new BigInteger(bytes);
        String bitString = bi.toString(BINAIR_RADIX);
        while (bitString.length() < (bytes.length * NR_OF_BITS_PER_BYTE)) {
            bitString = "0" + bitString;
        }
        return bitString;
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
        if (!Arrays.equals(this.getBytes(), that.getBytes())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }
}
