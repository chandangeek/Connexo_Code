/*
 * ActualLogTable.java
 *
 * Created on 17 november 2005, 11:41
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
public class ActualLogTable extends AbstractTable {

    private Log log;

    /** Creates a new instance of ActualLogTable */
    public ActualLogTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(71));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ActualLogTable: log="+getLog()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        setLog(new Log(tableData,0,getTableFactory()));
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }
}
