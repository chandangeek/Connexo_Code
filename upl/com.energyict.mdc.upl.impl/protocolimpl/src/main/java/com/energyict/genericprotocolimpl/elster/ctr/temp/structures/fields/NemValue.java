package com.energyict.genericprotocolimpl.elster.ctr.temp.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 13:39:29
 */
public class NemValue extends AbstractStructureField {

    public static final int LENGTH = 2;
    private final byte[] nemValue;

    public NemValue(byte[] rawPacket, int offset) {
        nemValue = new byte[LENGTH];
        for (int i = 0; i < nemValue.length; i++) {
            nemValue[i] = rawPacket[offset + i];
        }

    }

    public byte[] getBytes() {
        return nemValue;
    }
}
