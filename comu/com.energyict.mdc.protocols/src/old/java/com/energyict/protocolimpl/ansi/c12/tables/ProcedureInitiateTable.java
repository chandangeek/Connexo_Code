/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProcedureInitiateTable.java
 *
 * Created on 19 oktober 2005, 20:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.procedures.AbstractProcedure;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class ProcedureInitiateTable extends AbstractTable {

    private TableIDBBitfield tableIDBBitfield;
    private int sequenceNr; // byte
    private AbstractProcedure procedure; // procedureData, byte[]

    /** Creates a new instance of ProcedureInitiateTable */
    public ProcedureInitiateTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(7));
    }

    protected void parse(byte[] tableData) throws IOException {

    }

    protected void prepareTransfer() throws IOException {
        byte[] procedureData = getProcedure().getProcedureData();
        byte[] tableData = new byte[(procedureData==null?0:procedureData.length)+TableIDBBitfield.getSize()+1];
        int tableIdbBitfield = (getTableIDBBitfield().getProcedureNr()&0x07FF);
        tableIdbBitfield |= (getTableIDBBitfield().isStdVsMfgFlag()?0x0800:0x0000);
        tableIdbBitfield |= (getTableIDBBitfield().getSelector()<<12);
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (dataOrder == 0) { // least significant first
            tableData[0] = (byte)(tableIdbBitfield);
            tableData[1] = (byte)(tableIdbBitfield>>8);
            tableData[2] = (byte)getSequenceNr();
        }
        else if (dataOrder == 1) { // most significant first
            tableData[0] = (byte)(tableIdbBitfield>>8);
            tableData[1] = (byte)tableIdbBitfield;
            tableData[2] = (byte)getSequenceNr();
        }
        else throw new IOException("ProcedureInitiateTable, prepareTransfer(), invalid dataOrder "+dataOrder);

        if (procedureData!=null)
            System.arraycopy(procedureData,0,tableData,3,procedureData.length);
        setTableData(tableData);
    }

    public int getSequenceNr() {
        return sequenceNr;
    }

    public void setSequenceNr(int sequenceNr) {
        this.sequenceNr = sequenceNr;
    }

    public AbstractProcedure getProcedure() {
        return procedure;
    }

    public void setProcedure(AbstractProcedure procedure) {
        this.procedure = procedure;
    }

    public TableIDBBitfield getTableIDBBitfield() {
        return tableIDBBitfield;
    }

    public void setTableIDBBitfield(TableIDBBitfield tableIDBBitfield) {
        this.tableIDBBitfield = tableIDBBitfield;
    }



}
