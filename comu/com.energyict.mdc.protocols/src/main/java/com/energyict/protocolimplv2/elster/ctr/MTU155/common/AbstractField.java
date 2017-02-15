/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.common;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Arrays;

public abstract class AbstractField<T extends Field> implements Field<T> {

    protected int getIntFromBytes(byte[] rawData, int offset, int length) {
        byte[] intBytes = ProtocolTools.getSubArray(rawData, offset, offset + length);
        int value = 0;
        for (int i = 0; i < intBytes.length; i++) {
            int intByte = intBytes[i] & 0x0FF;
            value += intByte << ((intBytes.length - (i + 1)) * 8);
        }
        return value;
    }

    protected byte[] getBytesFromInt(int value, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            int ptr = (bytes.length - (i + 1));
            bytes[ptr] = (i < 4) ? (byte) ((value >> (i * 8))) : 0x00;
        }
        return bytes;
    }

    protected boolean isBitSet(int value, int bitNr) {
        return (0 != (value & (0x01 << bitNr)));
    }

    protected int setBit(int value, boolean bit, int bitNr) {
        int mask = 0x01 << bitNr;
        return bit ? (value | mask) : (value & (~mask));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " = " + ProtocolTools.getHexStringFromBytes(getBytes());
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

        if (!Arrays.equals(getBytes(), that.getBytes())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }


}
