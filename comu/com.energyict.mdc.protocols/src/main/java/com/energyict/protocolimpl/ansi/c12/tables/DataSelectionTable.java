/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataSelectionTable.java
 *
 * Created on 27 oktober 2005, 16:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DataSelectionTable extends AbstractTable {

    private int[] summationSelects;
    private int[] demandSelects;
    private int[] minOrMaxFlags;
    private int[] coincidentSelects;
    private int[] coinDemandAssocs;

    /** Creates a new instance of DataSelectionTable */
    public DataSelectionTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(22));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataSelectionTable: \n");
        for (int i=0;i<getSummationSelects().length;i++)
            strBuff.append("    summationSelects["+i+"]="+getSummationSelects()[i]+"\n");
        for (int i=0;i<getDemandSelects().length;i++)
            strBuff.append("    demandSelects["+i+"]="+getDemandSelects()[i]+"\n");
        for (int i=0;i<getMinOrMaxFlags().length;i++)
            strBuff.append("    minOrMaxFlags["+i+"]=0x"+Integer.toHexString(getMinOrMaxFlags()[i])+"\n");
        for (int i=0;i<getCoincidentSelects().length;i++)
            strBuff.append("    coincidentSelects["+i+"]="+getCoincidentSelects()[i]+"\n");
        for (int i=0;i<getCoinDemandAssocs().length;i++)
            strBuff.append("    coinDemandAssocs["+i+"]="+getCoinDemandAssocs()[i]+"\n");
        return strBuff.toString();
    }

    public Unit getUnit() {
        return null;
    }

    protected void parse(byte[] tableData) throws IOException {
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();

        setSummationSelects(new int[art.getNrOfSummations()]);
        setDemandSelects(new int[art.getNrOfDemands()]);
        setMinOrMaxFlags(new int[(art.getNrOfDemands()+7)/8]);
        setCoincidentSelects(new int[art.getNrOfCoinValues()]);
        setCoinDemandAssocs(new int[art.getNrOfCoinValues()]);
        int offset=0;
        for (int i=0;i<getSummationSelects().length;i++)
            getSummationSelects()[i] = C12ParseUtils.getInt(tableData,offset++);
        for (int i=0;i<getDemandSelects().length;i++)
            getDemandSelects()[i] = C12ParseUtils.getInt(tableData,offset++);
        for (int i=0;i<getMinOrMaxFlags().length;i++)
            getMinOrMaxFlags()[i] = C12ParseUtils.getInt(tableData,offset++);
        for (int i=0;i<getCoincidentSelects().length;i++)
            getCoincidentSelects()[i] = C12ParseUtils.getInt(tableData,offset++);
        for (int i=0;i<getCoinDemandAssocs().length;i++)
            getCoinDemandAssocs()[i] = C12ParseUtils.getInt(tableData,offset++);
    }

    public int[] getSummationSelects() {
        return summationSelects;
    }

    public void setSummationSelects(int[] summationSelects) {
        this.summationSelects = summationSelects;
    }

    public int[] getDemandSelects() {
        return demandSelects;
    }

    public void setDemandSelects(int[] demandSelects) {
        this.demandSelects = demandSelects;
    }

    public int[] getMinOrMaxFlags() {
        return minOrMaxFlags;
    }

    public void setMinOrMaxFlags(int[] minOrMaxFlags) {
        this.minOrMaxFlags = minOrMaxFlags;
    }

    public int[] getCoincidentSelects() {
        return coincidentSelects;
    }

    public void setCoincidentSelects(int[] coincidentSelects) {
        this.coincidentSelects = coincidentSelects;
    }

    public int[] getCoinDemandAssocs() {
        return coinDemandAssocs;
    }

    public void setCoinDemandAssocs(int[] coinDemandAssocs) {
        this.coinDemandAssocs = coinDemandAssocs;
    }

}
