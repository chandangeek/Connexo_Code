/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ActualTimeAndTOUTable.java
 *
 * Created on 2 november 2005, 10:21
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
public class ActualTimeAndTOUTable extends AbstractTable {

    private TimeTOU timeTOU;

    /** Creates a new instance of ActualTimeAndTOUTable */
    public ActualTimeAndTOUTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(51));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ActualTimeAndTOUTable: \n");
        strBuff.append("    timeTOU="+getTimeTOU()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        setTimeTOU(new TimeTOU(tableData, 0, getTableFactory()));
    }

    public TimeTOU getTimeTOU() {
        return timeTOU;
    }

    public void setTimeTOU(TimeTOU timeTOU) {
        this.timeTOU = timeTOU;
    }

}

