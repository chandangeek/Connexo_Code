package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 14:32:28
 */
public class IdentificationRequest extends AbstractCTRPacket {

    public IdentificationRequest(AddressField addressField) {
        super(addressField);
    }


    public AddressField getAddress() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public FunctionCode getFunctionCode() {
        return new FunctionCode('I');
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
