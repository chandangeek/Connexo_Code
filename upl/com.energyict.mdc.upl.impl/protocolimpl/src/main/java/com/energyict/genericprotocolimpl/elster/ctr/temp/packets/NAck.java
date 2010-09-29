package com.energyict.genericprotocolimpl.elster.ctr.temp.packets;

import com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields.Data;
import com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields.NAckData;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 10:02:24
 */
public class NAck extends AbstractCTRPacket {

    private final NAckData nAckData;

    public NAck(byte[] rawPacket) {
        super(rawPacket);
        this.nAckData = new NAckData(); //TODO: Add parsing of NACKDATA
    }

    public Data getData() {
        return nAckData;
    }
}
