/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterProgramConstants1.java
 *
 * Created on 10 november 2005, 14:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MeterProgramConstants1 extends AbstractTable {

    private int frequency; // 8 bit, 0=50Hz, 1=60Hz
    private int timeBase; // 8 bit, 0=line, 1=crystal
    private int kt; // 16 bit

    /** Creates a new instance of MeterProgramConstants1 */
    public MeterProgramConstants1(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(66,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterProgramConstants1: frequency="+getFrequency()+", timeBase="+getTimeBase()+", kt="+getKt()+"\n");
        return strBuff.toString();
    }

    protected void prepareTransfer() {

    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setFrequency(C12ParseUtils.getInt(tableData,0));
        setTimeBase(C12ParseUtils.getInt(tableData,1));
        setKt(C12ParseUtils.getInt(tableData,2,2,dataOrder));
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(int timeBase) {
        this.timeBase = timeBase;
    }

    public int getKt() {
        return kt;
    }

    public void setKt(int kt) {
        this.kt = kt;
    }
}
