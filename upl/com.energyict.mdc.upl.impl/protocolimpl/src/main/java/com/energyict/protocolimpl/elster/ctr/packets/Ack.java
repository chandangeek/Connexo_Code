package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.AckData;
import com.energyict.protocolimpl.elster.ctr.packets.fields.Data;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 10:00:01
 */
public class Ack extends AbstractCTRPacket {

    private final AckData ackData;

    public Ack(byte[] rawPacket) {
        super(rawPacket);
        ackData = new AckData(); // TODO: pase data
    }

    public Data getData() {
        return ackData;
    }
}
