package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 16:00:48
 */
public class EndOfSessionRequestStructure extends Data<EndOfSessionRequestStructure> {

    public EndOfSessionRequestStructure() {
        super(false);
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                new byte[0]
        ));
    }

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     * @throws CTRParsingException
     */
    @Override
    public EndOfSessionRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {
        return this;
    }

}
