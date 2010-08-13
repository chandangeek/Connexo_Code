package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

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

    public FunctionCode getFunctionCode() {
        return FunctionType.NACK.getFunctionCode();
    }

    public Data getData() {
        return nAckData;
    }
}
