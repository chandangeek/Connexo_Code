/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Readings.java
 *
 * Created on 8 november 2005, 14:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Readings {

    private Number blockEndRead;
    private long blockEndPulse;

    /** Creates a new instance of Readings */
    public Readings(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (alpt.getLoadProfileSet().isBlockEndReadFlag()) {
            setBlockEndRead(C12ParseUtils.getNumberFromNonInteger(data,offset, cfgt.getNonIntFormat1(), dataOrder));
            offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        }
        if (alpt.getLoadProfileSet().isBlockEndPulseFlag()) {
            setBlockEndPulse(C12ParseUtils.getLong(data,offset, 4, dataOrder));
            offset+=4;
        }
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Readings: blockEndRead="+getBlockEndRead()+", blockEndPulse="+getBlockEndPulse()+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        int size=0;
        if (alpt.getLoadProfileSet().isBlockEndReadFlag())
            size+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        if (alpt.getLoadProfileSet().isBlockEndPulseFlag())
            size+=4;
        return size;
    }

    public Number getBlockEndRead() {
        return blockEndRead;
    }

    public void setBlockEndRead(Number blockEndRead) {
        this.blockEndRead = blockEndRead;
    }

    public long getBlockEndPulse() {
        return blockEndPulse;
    }

    public void setBlockEndPulse(long blockEndPulse) {
        this.blockEndPulse = blockEndPulse;
    }
}
