/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ActualLoadProfileTable.java
 *
 * Created on 7 november 2005, 16:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ActualLoadProfileTable extends AbstractTable {

    private LoadProfileSet loadProfileSet;

    /** Creates a new instance of ActualLoadProfileTable */
    public ActualLoadProfileTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(61));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ActualLoadProfileTable: \n");
        strBuff.append("    loadProfileSet="+getLoadProfileSet()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        //ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        //ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        //ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int offset=0;
        setLoadProfileSet(new LoadProfileSet(tableData, offset,getTableFactory()));
    }

    public LoadProfileSet getLoadProfileSet() {
        return loadProfileSet;
    }

    public void setLoadProfileSet(LoadProfileSet loadProfileSet) {
        this.loadProfileSet = loadProfileSet;
    }
}
