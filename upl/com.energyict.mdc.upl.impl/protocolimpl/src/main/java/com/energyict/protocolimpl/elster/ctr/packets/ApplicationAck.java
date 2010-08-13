package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

/**
 * The message is used as an Acknowledgement or as consent from the recipient’s application
 * level to the application level of the sender. The message is not encrypted.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12-aug-2010
 * Time: 13:35:26
 */
public class ApplicationAck extends AbstractCTRPacket {

    public ApplicationAck(AddressField addressField) {
        super(addressField);
    }

    public ApplicationAck(byte[] rawPacket) {
        super(rawPacket);

    }

    public FunctionCode getFunctionCode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Data getData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
