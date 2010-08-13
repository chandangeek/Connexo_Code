package com.energyict.protocolimpl.elster.ctr.structures;

import com.energyict.protocolimpl.elster.ctr.structures.fields.*;

/**
 * Copyrights EnergyICT
 * Date: 12-aug-2010
 * Time: 10:55:59
 */
public class IdentificationStructure implements Structure {

    private final PDRValue pdrValue;
    private final AllPAValue allPAValue;
    private final NCGValue ncgValue;

    public IdentificationStructure(byte[] rawPacket, int structureOffset) {
        int offset = structureOffset;

        pdrValue = new PDRValue(rawPacket, offset);
        offset += PDRValue.LENGTH;

        allPAValue = new AllPAValue(rawPacket, offset);
        offset += AllPAValue.LENGTH;

        ncgValue = new NCGValue(rawPacket, offset);
        offset += NCGValue.LENGTH;

    }

    public byte[] getBytes() {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
