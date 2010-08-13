package com.energyict.protocolimpl.elster.ctr.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 11:02:45
 */
public class AllPAValue extends AbstractStructureField {

    public static final int LENGTH = 34;
    private final byte[] allPAValue;

    public AllPAValue(byte[] rawPacket, int fieldOffset) {
        int offset = fieldOffset;
        allPAValue = new byte[LENGTH];
        for (int i = 0; i < allPAValue.length; i++) {
            allPAValue[i] = rawPacket[offset++];
        }
    }

    public byte[] getBytes() {
        return allPAValue;
    }
}
