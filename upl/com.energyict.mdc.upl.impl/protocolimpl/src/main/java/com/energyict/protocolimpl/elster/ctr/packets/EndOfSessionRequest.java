package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 15:12:11
 */
public class EndOfSessionRequest extends AbstractCTRPacket {

    public EndOfSessionRequest(AddressField addressField) {
        super(addressField);
    }

    public FunctionCode getFunctionCode() {
        return new FunctionCode('E');
    }

    public StructureCode getStructureCode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Channel getChannel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Data getData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
