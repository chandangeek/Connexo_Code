package com.energyict.genericprotocolimpl.elster.ctr.temp.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 13:39:29
 */
public class AnContValue extends AbstractStructureField {

    public static final int LENGTH = 20;
    private final byte[] anContValue;

    public AnContValue(byte[] rawPacket, int offset) {
        anContValue = new byte[LENGTH];
        for (int i = 0; i < anContValue.length; i++) {
            anContValue[i] = rawPacket[offset + i];
        }

    }

    public byte[] getBytes() {
        return anContValue;
    }
}
