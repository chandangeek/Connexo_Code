/*
 * DimensionTimeAndTOUTable.java
 *
 * Created on 2 november 2005, 9:40
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
public class DimensionTimeAndTOUTable extends AbstractTable {

    TimeTOU timeTOU;

    /** Creates a new instance of DimensionTimeAndTOUTable */
    public DimensionTimeAndTOUTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(50));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DimensionTimeAndTOUTable: \n");
        strBuff.append("    timeTOU="+timeTOU+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        timeTOU = new TimeTOU(tableData, 0, getTableFactory());
    }

}
