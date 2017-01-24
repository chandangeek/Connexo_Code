package com.energyict.protocolimplv2.elster.ctr.MTU155.frame;

import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;

/**
 * Copyrights EnergyICT
 * Date: 27-sep-2010
 * Time: 8:36:04
 */
public class SMSFrame extends AbstractCTRFrame<SMSFrame> {

    private WriteDataBlock wdb;

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

    public WriteDataBlock getWdb() {
        return wdb;
    }

    // Sets the WriteDataBlock that is used in the frames data (E.g.: in the RegisterWriteRequestStructure).
    public void setWriteDataBlock(WriteDataBlock wdb) {
        this.wdb = wdb;
    }
}
