/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PreviousIntervalDemand.java
 *
 * Created on 11 februari 2006, 15:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SummationSnapshotTable extends AbstractTable { 
    /*
    Memory storage: RAM
    Total table size: (bytes) 49, Fixed
    Read access: 1 (must be in Cellnet access group)
    Write access: N/A
    Owner class: TariffData

    This table is provided for AMR applications that want to use summation data to calculate load profile data. 
    The meter will snap-shot the ST-23 summations at the end of each demand subinterval.
    This table is sized for the maximum number of summations. Disabled summations will report a summation value of '0'.
    */

    int sequenceNumber; // 1 byte This number will be incremented each time a snapshot is taken of the summations.
                        // This allows option boards to check if they have missed a reading.
    Number[] summations; // 8 Numbers following number format 1

    /** Creates a new instance of PreviousIntervalDemand */
    public SummationSnapshotTable(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(23,true));
    }
 
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SummationSnapshot:\n");
        strBuff.append("    sequenceNumber="+sequenceNumber+"\n");
        for (int i=0;i<summations.length;i++) {
            strBuff.append("    summations["+i+"]="+summations[i]+"\n");
        }    
        return strBuff.toString();
    }
    
    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int offset = 0;
        sequenceNumber = C12ParseUtils.getInt(tableData,offset++);
        summations = new Number[8];
        for (int i=0;i<summations.length;i++) {
            summations[i] = C12ParseUtils.getNumberFromNonInteger(tableData, offset, cfgt.getNonIntFormat1(),dataOrder);
            offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        }
    } 
    
    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }
    
//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }


}
