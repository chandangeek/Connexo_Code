/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

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
