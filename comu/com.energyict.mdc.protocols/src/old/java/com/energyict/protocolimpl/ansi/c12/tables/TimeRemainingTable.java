/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TimeRemainingTable.java
 *
 * Created on 7 november 2005, 15:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TimeRemainingTable extends AbstractTable {

    private int summTierTimeRemain=-1;
    private int demandTierTimeRemain=-1;
    private int tierTimeRemain=-1;
    private int selfReadDaysRemain;

    /** Creates a new instance of TimeRemainingTable */
    public TimeRemainingTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(56));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TimeRemainingTable: summTierTimeRemain="+getSummTierTimeRemain()+", demandTierTimeRemain="+getDemandTierTimeRemain()+", tierTimeRemain="+getTierTimeRemain()+", selfReadDaysRemain="+getSelfReadDaysRemain()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        int offset=0;
        if (atatt.getTimeTOU().isSeparateSumDemandsFlag()) {
            setSummTierTimeRemain(C12ParseUtils.getInt(tableData,offset, 2,dataOrder));
            offset+=2;
            setDemandTierTimeRemain(C12ParseUtils.getInt(tableData,offset, 2,dataOrder));
            offset+=2;
        }
        else {
            setTierTimeRemain(C12ParseUtils.getInt(tableData,offset, 2,dataOrder));
            offset+=2;
        }

        setSelfReadDaysRemain(C12ParseUtils.getInt(tableData,offset));

    }

    public int getSummTierTimeRemain() {
        return summTierTimeRemain;
    }

    public void setSummTierTimeRemain(int summTierTimeRemain) {
        this.summTierTimeRemain = summTierTimeRemain;
    }

    public int getDemandTierTimeRemain() {
        return demandTierTimeRemain;
    }

    public void setDemandTierTimeRemain(int demandTierTimeRemain) {
        this.demandTierTimeRemain = demandTierTimeRemain;
    }

    public int getTierTimeRemain() {
        return tierTimeRemain;
    }

    public void setTierTimeRemain(int tierTimeRemain) {
        this.tierTimeRemain = tierTimeRemain;
    }

    public int getSelfReadDaysRemain() {
        return selfReadDaysRemain;
    }

    public void setSelfReadDaysRemain(int selfReadDaysRemain) {
        this.selfReadDaysRemain = selfReadDaysRemain;
    }
}
