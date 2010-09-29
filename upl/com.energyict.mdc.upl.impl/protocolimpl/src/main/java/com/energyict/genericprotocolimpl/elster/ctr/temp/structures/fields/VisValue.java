package com.energyict.genericprotocolimpl.elster.ctr.temp.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 13:39:29
 */
public class VisValue extends AbstractStructureField {

    public static final int LENGTH = 4;
    private final byte[] visValue;

    public VisValue(byte[] rawPacket, int offset) {
        visValue = new byte[LENGTH];
        for (int i = 0; i < visValue.length; i++) {
            visValue[i] = rawPacket[offset + i];
        }

    }

    public byte[] getBytes() {
        return visValue;
    }
}
