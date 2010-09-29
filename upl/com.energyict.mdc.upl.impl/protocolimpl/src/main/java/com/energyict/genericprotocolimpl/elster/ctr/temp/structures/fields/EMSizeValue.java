package com.energyict.genericprotocolimpl.elster.ctr.temp.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 13:33:49
 */
public class EMSizeValue extends AbstractStructureField {

    public static final int LENGTH = 3;

    private final byte[] emSizeValue;

    public EMSizeValue(byte[] rawPacket, int offset) {
        emSizeValue = new byte[LENGTH];
        for (int i = 0; i < emSizeValue.length; i++) {
            emSizeValue[i] = rawPacket[i + offset];
        }
    }

    public byte[] getBytes() {
        return emSizeValue;
    }
}
