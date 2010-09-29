package com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields;

import com.energyict.genericprotocolimpl.elster.ctr.temp.structures.IdentificationStructure;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 10:34:51
 */
public class IdentificationResponseData extends Data {

    private final IdentificationStructure identificationStructure;

    public IdentificationResponseData(byte[] rawPacket, int offset) {
        this.identificationStructure = new IdentificationStructure(rawPacket, offset);
    }

    @Override
    public String toString() {
        return identificationStructure.toString();
    }
}
