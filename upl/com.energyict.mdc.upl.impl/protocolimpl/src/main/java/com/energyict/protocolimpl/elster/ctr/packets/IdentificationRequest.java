package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 14:32:28
 */
public class IdentificationRequest extends AbstractCTRPacket {

    private final IdentificationRequestData identificationRequestData;

    public IdentificationRequest(AddressField addressField) {
        super(addressField);
        identificationRequestData = new IdentificationRequestData();
    }

    public FunctionCode getFunctionCode() {
        return new FunctionCode('I');
    }

    @Override
    public StructureCode getStructureCode() {
        return new StructureCode(0x30);
    }

    @Override
    public Aleo getAleo() {
        return new Aleo(0);
    }

    public Data getData() {
        return identificationRequestData;
    }
    
}
