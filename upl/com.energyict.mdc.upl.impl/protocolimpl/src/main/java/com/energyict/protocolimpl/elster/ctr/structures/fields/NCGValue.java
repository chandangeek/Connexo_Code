package com.energyict.protocolimpl.elster.ctr.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 11:06:48
 */
public class NCGValue extends AbstractStructureField {

    public static final int LENGTH = 1;
    private final byte[] ncgValue;

    public NCGValue(byte[] rawPacket, int fieldOffset) {
        ncgValue = new byte[]{rawPacket[fieldOffset]};
    }

    public byte[] getBytes() {
        return ncgValue;
    }
}
