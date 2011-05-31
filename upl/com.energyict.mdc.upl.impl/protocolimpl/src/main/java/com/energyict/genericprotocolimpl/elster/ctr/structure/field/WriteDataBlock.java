package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Class for the WriteDataBlock field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 11:15:26
 */
public class WriteDataBlock extends AbstractField<WriteDataBlock> {

    private int wdb;
    private static final int LENGTH = 1;

    public WriteDataBlock(int wdb) {
        this.wdb = wdb;
    }

    public WriteDataBlock() {
    }

    public int getWdb() {
        return wdb;
    }

    public void setWdb(int wdb) {
        this.wdb = wdb;
    }

    public byte[] getBytes() {
        return getBytesFromInt(getWdb(), LENGTH);
    }

    public WriteDataBlock parse(byte[] rawData, int offset) throws CTRParsingException {
        setWdb(getIntFromBytes(rawData, offset, LENGTH));
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

    public static WriteDataBlock getRandomWDB() {
        return new WriteDataBlock((int) (100 * Math.random()));
    }

}
