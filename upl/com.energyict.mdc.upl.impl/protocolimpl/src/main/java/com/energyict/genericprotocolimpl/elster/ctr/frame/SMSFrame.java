package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 27-sep-2010
 * Time: 8:36:04
 */
public class SMSFrame extends AbstractCTRFrame<SMSFrame> {

    /**
     * Parses a given byte array into an SMSFrame object
     * @param rawData: the byte array
     * @param offset: position to start in the byte array
     * @return the SMSFrame object
     * @throws CTRParsingException
     */
    @Override
    public SMSFrame parse(byte[] rawData, int offset) throws CTRParsingException {
        super.parse(rawData, offset);
        return this;
    }
    
}
