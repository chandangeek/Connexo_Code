package com.energyict.protocolimpl.elster.ctr.structures.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 13:39:29
 */
public class PucS extends AbstractStructureField {

    public static final int LENGTH = 16;
    private final byte[] pucS;

    public PucS(byte[] rawPacket, int offset) {
        pucS = new byte[LENGTH];
        for (int i = 0; i < pucS.length; i++) {
            pucS[i] = rawPacket[offset + i];
        }

    }

    public byte[] getBytes() {
        return pucS;
    }
}
