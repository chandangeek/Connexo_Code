package com.energyict.genericprotocolimpl.elster.ctr.common;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 17:24:22
 */
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

}
