/*
 * ActualTelephoneTable.java
 *
 * Created on 23 februari 2006, 11:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ActualTelephoneTable extends AbstractTable {

    private TelephoneRecord telephoneRecord;

    /** Creates a new instance of ActualTelephoneTable */
    public ActualTelephoneTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(91));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DimensionTelephoneTable: \n");
        strBuff.append("    telephoneRecord="+getTelephoneRecord()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLogTable alt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        telephoneRecord = new TelephoneRecord(tableData,0,getTableFactory());
    }

    public TelephoneRecord getTelephoneRecord() {
        return telephoneRecord;
    }
}
