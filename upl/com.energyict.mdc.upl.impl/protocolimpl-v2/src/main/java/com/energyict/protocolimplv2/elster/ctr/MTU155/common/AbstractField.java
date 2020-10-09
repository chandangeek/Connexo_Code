package com.energyict.protocolimplv2.elster.ctr.MTU155.common;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Arrays;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 17:24:22
 */
public abstract class AbstractField<T extends Field> implements Field<T> {

    protected int getIntFromBytes(byte[] rawData, int offset, int length) {
        return ProtocolTools.getIntFromBytes(rawData, offset, length);
    }

    protected byte[] getBytesFromInt(int value, int length) {
       return ProtocolTools.getBytesFromInt(value, length);
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
