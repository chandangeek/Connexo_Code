/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SelfReadDataTable.java
 *
 * Created on 28 oktober 2005, 16:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SelfReadDataTable extends AbstractTable {

    private SelfReadList selfReadList;

    /** Creates a new instance of SelfReadDataTable */
    public SelfReadDataTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(26));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadDataTable: \n");
        strBuff.append("    selfReadList="+getSelfReadList()+"\n");
        return strBuff.toString();
    }

    protected void prepareBuild() throws IOException {
        PartialReadInfo partialReadInfo = new PartialReadInfo(0,SelfReadList.getSize(getTableFactory()));
        setPartialReadInfo(partialReadInfo);
    }

    protected void parse(byte[] tableData) throws IOException {
        setSelfReadList(new SelfReadList(tableData, 0, getTableFactory()));
    }

    public SelfReadList getSelfReadList() {
        return selfReadList;
    }

    public void setSelfReadList(SelfReadList selfReadList) {
        this.selfReadList = selfReadList;
    }
}
