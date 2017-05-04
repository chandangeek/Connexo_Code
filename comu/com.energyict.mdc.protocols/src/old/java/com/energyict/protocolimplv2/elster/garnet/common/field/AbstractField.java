/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        byte[] intBytes = ProtocolTools.getSubArray(rawData, offset, offset + length);
        int value = 0;
        for (int i = 0; i < intBytes.length; i++) {
            int intByte = intBytes[i] & 0x0FF;
            value += intByte << ((intBytes.length - (i + 1)) * 8);
        }
        return value;
    }

    protected int getIntFromBytesLE(byte[] rawData, int offset, int length) {
        byte[] intBytes = ProtocolTools.getSubArray(rawData, offset, offset + length);
        int value = 0;
        for (int i = 0; i < intBytes.length; i++) {
            int intByte = intBytes[i] & 0x0FF;
            value += intByte << (i * 8);
        }
        return value;
    }

    protected String getHexStringFromBCD(byte[] rawData, int offset, int length) throws ParsingException {
        return ProtocolTools.getHexStringFromBytes(ProtocolTools.getSubArray(rawData, offset, offset + length), "");
    }

    protected byte[] getBytesFromInt(int value, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            int ptr = (bytes.length - (i + 1));
            bytes[ptr] = (i < 4) ? (byte) ((value >> (i * 8))) : 0x00;
        }
        return bytes;
    }

    protected byte[] getBytesFromIntLE(int value, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (i < 4) ? (byte) ((value >> (i * 8))) : 0x00;
        }
        return bytes;
    }

    protected byte[] getBCDFromHexString(String hexString, int length) {
        while (hexString.length() < (length * 2)) {
            hexString = "0" + hexString;    // Left pad with 0
        }
        return ProtocolTools.getBytesFromHexString(hexString, "");
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
