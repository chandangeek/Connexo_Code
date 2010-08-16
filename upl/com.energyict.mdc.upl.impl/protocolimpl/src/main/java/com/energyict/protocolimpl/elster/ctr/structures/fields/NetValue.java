package com.energyict.protocolimpl.elster.ctr.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 13:39:29
 */
public class NetValue extends AbstractStructureField {

    public static final int LENGTH = 1;
    private final byte netValue;

    public NetValue(byte[] rawPacket, int offset) {
        netValue = rawPacket[offset];
    }

    public byte[] getBytes() {
        return new byte[]{netValue};
    }
}
