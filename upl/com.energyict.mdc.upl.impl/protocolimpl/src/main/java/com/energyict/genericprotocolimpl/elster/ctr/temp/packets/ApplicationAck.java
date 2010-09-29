package com.energyict.genericprotocolimpl.elster.ctr.temp.packets;

import com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields.*;

/**
 * The message is used as an Acknowledgement or as consent from the recipientís application
 * level to the application level of the sender. The message is not encrypted.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12-aug-2010
 * Time: 13:35:26
 */
public class ApplicationAck extends AbstractCTRPacket {

    public ApplicationAck(AddressField addressField) {
        super(FunctionType.ACK, addressField);
    }

    public ApplicationAck(byte[] rawPacket) {
        super(rawPacket);

    }

    public Data getData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
