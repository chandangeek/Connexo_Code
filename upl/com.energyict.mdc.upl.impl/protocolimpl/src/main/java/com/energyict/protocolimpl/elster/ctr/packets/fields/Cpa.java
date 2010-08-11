package com.energyict.protocolimpl.elster.ctr.packets.fields;

import com.energyict.protocolimpl.base.CRC16DNP;
import com.energyict.protocolimpl.elster.ctr.packets.PacketField;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:44:49
 */
public class Cpa implements PacketField {

    private final byte[] cpaValue;

    public Cpa(Data dataField) {
        cpaValue = CRC16DNP.calcCRCAsBytes(dataField.getBytes());
    }

    public byte[] getBytes() {
        return cpaValue;
    }
    
}
