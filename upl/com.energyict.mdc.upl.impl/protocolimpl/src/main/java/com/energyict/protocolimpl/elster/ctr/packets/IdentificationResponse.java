package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.Data;
import com.energyict.protocolimpl.elster.ctr.packets.fields.IdentificationResponseData;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 9:21:48
 */
public class IdentificationResponse extends AbstractCTRPacket {

    private final IdentificationResponseData identificationResponseData;

    public IdentificationResponse(byte[] rawPacket) {
        super(rawPacket);
        identificationResponseData = new IdentificationResponseData(rawPacket, getDataOffset());
    }

    public Data getData() {
        return identificationResponseData;
    }

}
