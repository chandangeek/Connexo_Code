/*
 * ProcedureResponseTable.java
 *
 * Created on 24 oktober 2005, 9:31
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ProcedureResponseTable extends AbstractTable {
    
    
    private TableIDBBitfield tableIDBBitfield;
    
    private int resultCode; //byte
    private int sequenceNr; // byte
    private byte[] procedureResponseData; // byte[]
    
    /** Creates a new instance of ProcedureResponseTable */
    public ProcedureResponseTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(8));
    }
    
    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        
        setTableIDBBitfield(new TableIDBBitfield(tableData, 0,  dataOrder));
        setSequenceNr(C12ParseUtils.getInt(tableData,2));
        setResultCode(C12ParseUtils.getInt(tableData,3)); 
        // KV meter has selector != 2 but no response data for the procedure
        // e.g. setdatetime, no response data...
        if ((tableData.length-4)>0)
            setProcedureResponseData(ProtocolUtils.getSubArray2(tableData, 4, tableData.length-4));
    }


    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getSequenceNr() {
        return sequenceNr;
    }

    public void setSequenceNr(int sequenceNr) {
        this.sequenceNr = sequenceNr;
    }

    public byte[] getProcedureResponseData() {
        return procedureResponseData;
    }

    public void setProcedureResponseData(byte[] procedureResponseData) {
        this.procedureResponseData = procedureResponseData;
    }

    public TableIDBBitfield getTableIDBBitfield() {
        return tableIDBBitfield;
    }

    public void setTableIDBBitfield(TableIDBBitfield tableIDBBitfield) {
        this.tableIDBBitfield = tableIDBBitfield;
    }

}
