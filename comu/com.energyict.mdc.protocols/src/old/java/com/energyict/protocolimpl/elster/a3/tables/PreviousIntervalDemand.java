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
import com.energyict.protocolimpl.ansi.c12.tables.ActualRegisterTable;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PreviousIntervalDemand extends AbstractTable {

    /*
    Memory storage: RAM
    Total table size: (bytes) 50 max, dynamic (ST-21 same dependencies as demand portion of ST-27)
    Read access: 1
    Write access: N/A
    Owner class: PresentDemands

    This table contains the previous interval demand.
    The current interval demand is stored in ST-28.
    This table contains the previous interval values from ST-28.
    */

    Number[] previousIntervalDemands; // NI_FMAT2*10 One NI_FMAT2 entry for each of the demands. Contains the demand for the previous demand interval.

    /** Creates a new instance of PreviousIntervalDemand */
    public PreviousIntervalDemand(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(14,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PreviousIntervalDemand:\n");
        for (int i=0;i<previousIntervalDemands.length;i++) {
            strBuff.append("     previousIntervalDemands["+i+"]="+previousIntervalDemands[i]+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();

        int offset = 0;
        previousIntervalDemands = new Number[art.getNrOfDemands()];
        for (int i=0;i<previousIntervalDemands.length;i++) {
            previousIntervalDemands[i] = C12ParseUtils.getNumberFromNonInteger(tableData, offset, cfgt.getNonIntFormat2(),dataOrder);
            offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat2());
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
