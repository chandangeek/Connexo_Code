package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

/**
 * This message is used as a “NOT-Acknowledgement” or non-acceptance from the recipient’s
 * application level to the application level of the sender. The message is not encrypted.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12-aug-2010
 * Time: 13:35:26
 */
public class ApplicationNack extends AbstractCTRPacket {

    public ApplicationNack(AddressField addressField) {
        super(FunctionType.NACK, addressField);
    }

    public ApplicationNack(byte[] rawPacket) {
        super(rawPacket);
    }

    public Data getData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
