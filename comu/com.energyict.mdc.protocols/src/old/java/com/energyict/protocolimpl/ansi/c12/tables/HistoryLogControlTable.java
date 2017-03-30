/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HistoryLogControlTable.java
 *
 * Created on 17 november 2005, 14:01
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
public class HistoryLogControlTable extends AbstractTable {

    private HistoryCtrl historyCtrl;


    /** Creates a new instance of HistoryLogControlTable */
    public HistoryLogControlTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(73));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("HistoryLogControlTable: historyCtrl="+getHistoryCtrl()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        setHistoryCtrl(new HistoryCtrl(tableData,0,getTableFactory()));
    }

    public HistoryCtrl getHistoryCtrl() {
        return historyCtrl;
    }

    public void setHistoryCtrl(HistoryCtrl historyCtrl) {
        this.historyCtrl = historyCtrl;
    }

}
