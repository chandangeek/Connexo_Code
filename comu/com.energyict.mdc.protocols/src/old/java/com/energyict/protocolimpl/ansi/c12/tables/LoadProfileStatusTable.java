/*
 * LoadProfileStatusTable.java
 *
 * Created on 8 november 2005, 11:10
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
public class LoadProfileStatusTable extends AbstractTable {

    private LoadProfileSetStatus loadProfileSet1Status;
    private LoadProfileSetStatus loadProfileSet2Status;
    private LoadProfileSetStatus loadProfileSet3Status;
    private LoadProfileSetStatus loadProfileSet4Status;

    /** Creates a new instance of LoadProfileStatusTable */
    public LoadProfileStatusTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(63));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileStatusTable: \n");
        strBuff.append("    loadProfileSet1Status="+getLoadProfileSet1Status()+"\n");
        strBuff.append("    loadProfileSet2Status="+getLoadProfileSet2Status()+"\n");
        strBuff.append("    loadProfileSet3Status="+getLoadProfileSet3Status()+"\n");
        strBuff.append("    loadProfileSet4Status="+getLoadProfileSet4Status()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        //ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        //ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int offset=0;

        if ((cfgt.getStdTablesUsed()[8]&0x01) == 0x01) {
            setLoadProfileSet1Status(new LoadProfileSetStatus(tableData,offset,getTableFactory()));
            offset += LoadProfileSetStatus.getSize(getTableFactory());
        }
        if ((cfgt.getStdTablesUsed()[8]&0x02) == 0x02) {
            setLoadProfileSet2Status(new LoadProfileSetStatus(tableData,offset,getTableFactory()));
            offset += LoadProfileSetStatus.getSize(getTableFactory());
        }
        if ((cfgt.getStdTablesUsed()[8]&0x04) == 0x04) {
            setLoadProfileSet3Status(new LoadProfileSetStatus(tableData,offset,getTableFactory()));
            offset += LoadProfileSetStatus.getSize(getTableFactory());
        }
        if ((cfgt.getStdTablesUsed()[8]&0x08) == 0x08) {
            setLoadProfileSet4Status(new LoadProfileSetStatus(tableData,offset,getTableFactory()));
            offset += LoadProfileSetStatus.getSize(getTableFactory());
        }

    }

    public LoadProfileSetStatus getLoadProfileSet1Status() {
        return loadProfileSet1Status;
    }

    public void setLoadProfileSet1Status(LoadProfileSetStatus loadProfileSet1Status) {
        this.loadProfileSet1Status = loadProfileSet1Status;
    }

    public LoadProfileSetStatus getLoadProfileSet2Status() {
        return loadProfileSet2Status;
    }

    public void setLoadProfileSet2Status(LoadProfileSetStatus loadProfileSet2Status) {
        this.loadProfileSet2Status = loadProfileSet2Status;
    }

    public LoadProfileSetStatus getLoadProfileSet3Status() {
        return loadProfileSet3Status;
    }

    public void setLoadProfileSet3Status(LoadProfileSetStatus loadProfileSet3Status) {
        this.loadProfileSet3Status = loadProfileSet3Status;
    }

    public LoadProfileSetStatus getLoadProfileSet4Status() {
        return loadProfileSet4Status;
    }

    public void setLoadProfileSet4Status(LoadProfileSetStatus loadProfileSet4Status) {
        this.loadProfileSet4Status = loadProfileSet4Status;
    }
}
