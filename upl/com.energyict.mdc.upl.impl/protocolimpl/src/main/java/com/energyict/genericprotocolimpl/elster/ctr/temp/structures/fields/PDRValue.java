package com.energyict.genericprotocolimpl.elster.ctr.temp.structures.fields;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 10:46:14
 */
public class PDRValue extends AbstractStructureField {

    public static final int LENGTH = 7;
    private final byte[] pdrValue;

    public PDRValue(byte[] rawPacket, int fieldOffset) {
        int offset = fieldOffset;
        pdrValue = new byte[LENGTH];
        for (int i = 0; i < pdrValue.length; i++) {
            pdrValue[i] = rawPacket[offset++];
        }
    }

    public String getStringValue() {
        return ProtocolTools.getHexStringFromBytes(getBytes()).replace("$", "");
    }

    public byte[] getBytes() {
        return pdrValue;
    }

    @Override
    public String toString() {
        return "PDRValue=" + getStringValue();
    }
}
