package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

/**
 * This procedure enables the Client to identify the Server. It is a special query on
 * TABLE structures, which does not require a password to be provided. Generally it is
 * the first message in a connection with data communication. The response is always required,
 * whatever the profile. The identification procedure is not mandatory, e.g. if the Client
 * already knows the Server with which it wishes to interact (such as the active client side caller ID function).
 * The message is not encrypted
 * <p/>
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

    public IdentificationRequest(byte[] rawPacket) {
        super(rawPacket);
        int offset = getDataOffset();
        this.identificationRequestData = new IdentificationRequestData(rawPacket, offset);
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
