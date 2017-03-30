/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TableIDBBitfield.java
 *
 * Created on 17 november 2005, 14:36
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
public class TableIDBBitfield {

    private int procedureNr;        // TABLE_IDB_BFLD_SIZE bit 10..0
    private boolean stdVsMfgFlag;   // TABLE_IDB_BFLD_SIZE bit 11
    private int selector;           // TABLE_IDB_BFLD_SIZE bit 15..12

    public TableIDBBitfield(int procedureNr,boolean stdVsMfgFlag,int selector) {
        this.procedureNr=procedureNr;
        this.stdVsMfgFlag=stdVsMfgFlag;
        this.selector=selector;
    }

    /** Creates a new instance of TableIDBBitfield */
    public TableIDBBitfield(byte[] data,int offset, int dataOrder) throws IOException {
        int tableIdbBitfield = C12ParseUtils.getInt(data,offset,2, dataOrder);
        setProcedureNr(tableIdbBitfield & 0x07FF);
        setStdVsMfgFlag((tableIdbBitfield & 0x0800) == 0x0800);
        setSelector((tableIdbBitfield & 0xF000)>>12);
    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TableIDBBitfield: procedureNr="+procedureNr+", stdVsMfgFlag="+stdVsMfgFlag+", selector="+selector+"\n");
        return strBuff.toString();

    }

    static public int getSize() throws IOException {
        return 2;
    }

    public int getProcedureNr() {
        return procedureNr;
    }

    public void setProcedureNr(int procedureNr) {
        this.procedureNr = procedureNr;
    }

    public boolean isStdVsMfgFlag() {
        return stdVsMfgFlag;
    }

    public void setStdVsMfgFlag(boolean stdVsMfgFlag) {
        this.stdVsMfgFlag = stdVsMfgFlag;
    }

    public int getSelector() {
        return selector;
    }

    public void setSelector(int selector) {
        this.selector = selector;
    }
}
