package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.protocolimpl.utils.ProtocolTools;

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
