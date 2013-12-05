/*
 * EventLogControlTable.java
 *
 * Created on 17 november 2005, 16:18
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
public class EventLogControlTable extends AbstractTable {

    private HistoryCtrl historyCtrl;

    /** Creates a new instance of EventLogControlTable */
    public EventLogControlTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(75));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventLogControlTable: historyCtrl="+getHistoryCtrl()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        setHistoryCtrl(new HistoryCtrl(tableData, 0, getTableFactory()));
    }

    public HistoryCtrl getHistoryCtrl() {
        return historyCtrl;
    }

    public void setHistoryCtrl(HistoryCtrl historyCtrl) {
        this.historyCtrl = historyCtrl;
    }
}
