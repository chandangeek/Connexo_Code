package com.energyict.genericprotocolimpl.elster.ctr.temp.packets;

import com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields.Data;
import com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields.IdentificationResponseData;

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
