package com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields;

import com.energyict.protocolimpl.base.CRC16DNP;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:44:49
 */
public class Cpa extends AbstractPacketField {

    public static final int LENGTH = 2;

    private final byte[] cpaValue;

    public Cpa(Data dataField) {
        cpaValue = CRC16DNP.calcCRCAsBytes(dataField.getBytes());
    }

    public Cpa(byte[] rawPacket, int offset) {
        cpaValue = new byte[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            cpaValue[i] = rawPacket[offset + i];
        }
    }

    public byte[] getBytes() {
        return cpaValue;
    }
    
}
