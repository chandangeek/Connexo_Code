package com.energyict.protocolimpl.elster.ctr.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 13:39:29
 */
public class SDValue extends AbstractStructureField {

    public static final int LENGTH = 1;
    private final byte sdValue;

    public SDValue(byte[] rawPacket, int offset) {
        sdValue = rawPacket[offset];
    }

    public byte[] getBytes() {
        return new byte[]{sdValue};
    }
}
