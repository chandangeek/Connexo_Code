/*
 * DataFactory.java
 *
 * Created on 23 juni 2006, 15:38
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran.core;

import com.energyict.protocolimpl.edf.trimaran.Trimaran;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DataFactory {

    private Trimaran trimeran;

    MonthInfoTable currentMonthInfoTable=null;
    MonthInfoTable previousMonthInfoTable=null;
    MeterStatusTable meterStatusTable=null;

    /** Creates a new instance of DataFactory */
    public DataFactory(Trimaran trimeran) {
        this.trimeran=trimeran;
    }

    public MeterStatusTable getMeterStatusTable() throws IOException {
        if (meterStatusTable==null) {
            meterStatusTable = new MeterStatusTable(this);
            meterStatusTable.setLength(30);
            meterStatusTable.invoke();
        }
        return meterStatusTable;
    }

    public MonthInfoTable getCurrentMonthInfoTable() throws IOException {
        if (currentMonthInfoTable==null) {
            currentMonthInfoTable = new MonthInfoTable(this);
            currentMonthInfoTable.setCode(2);
            currentMonthInfoTable.setLength(81);
            currentMonthInfoTable.invoke();
        }
        return currentMonthInfoTable;
    }

    public MonthInfoTable getPreviousMonthInfoTable() throws IOException {
        if (previousMonthInfoTable==null) {
            previousMonthInfoTable = new MonthInfoTable(this);
            previousMonthInfoTable.setCode(1);
            previousMonthInfoTable.setLength(81);
            previousMonthInfoTable.invoke();
        }
        return previousMonthInfoTable;
    }

    public DemandData getDemandData() throws IOException {
        DemandData dv = new DemandData(this);
        dv.setLength(4096);
        dv.invoke();
        return dv;
    }

    public Trimaran getTrimeran() {
        return trimeran;
    }

    private void setTrimeran(Trimaran trimeran) {
        this.trimeran = trimeran;
    }


}
