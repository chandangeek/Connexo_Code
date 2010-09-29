package com.energyict.genericprotocolimpl.elster.ctr.temp.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 13:39:29
 */
public class NeaValue extends AbstractStructureField {

    public static final int LENGTH = 2;
    private final byte[] neaValue;

    public NeaValue(byte[] rawPacket, int offset) {
        neaValue = new byte[LENGTH];
        for (int i = 0; i < neaValue.length; i++) {
            neaValue[i] = rawPacket[offset + i];
        }

    }

    public byte[] getBytes() {
        return neaValue;
    }
}
