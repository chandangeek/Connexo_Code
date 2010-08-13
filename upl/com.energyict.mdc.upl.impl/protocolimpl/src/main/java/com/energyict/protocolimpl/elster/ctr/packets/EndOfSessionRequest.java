package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

/**
 * The Client asks to terminate the communication session. The message is not encrypted.
 * The command is not mandatory for SMS communication.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 15:12:11
 */
public class EndOfSessionRequest extends AbstractCTRPacket {

    private final EndOfSessionRequestData endOfSessionRequestData = new EndOfSessionRequestData();

    public EndOfSessionRequest(AddressField addressField) {
        super(addressField);
    }

    public EndOfSessionRequest(byte[] rawPacket) {
        super(rawPacket);
    }

    public FunctionCode getFunctionCode() {
        return new FunctionCode('E');
    }

    public Data getData() {
        return endOfSessionRequestData;
    }

}
