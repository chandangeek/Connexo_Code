/*
 * DataFactory.java
 *
 * Created on 23 juni 2006, 15:38
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.core;

import java.io.IOException;

import com.energyict.protocolimpl.edf.trimarancje.Trimaran;

/**
 *
 * @author Koen
 */
public class DataFactory {
    
    private Trimaran trimaran;
    
    MonthInfoTable currentMonthInfoTable = null;
    MonthInfoTable previousMonthInfoTable = null;
    MeterStatusTable meterStatusTable = null;
    ContractsTable contractsTable = null;
    CurrentPeriodTable currentPeriodTable = null;
    
    /** Creates a new instance of DataFactory */
    public DataFactory(Trimaran trimaran) {
        this.trimaran=trimaran;
    }

	public ContractsTable getContractsTable() throws IOException {
		if(contractsTable == null){
			contractsTable = new ContractsTable(this);
			contractsTable.setLength(26);
			contractsTable.invoke();
		}
		return contractsTable;
	}
	
	public CurrentPeriodTable getCurrentPeriodTable() throws IOException {
		if(currentPeriodTable == null){
			currentPeriodTable = new CurrentPeriodTable(this);
			currentPeriodTable.setLength(61);
			currentPeriodTable.invoke();
		}
		return currentPeriodTable;
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
//        dv.setLength(4096);
//        dv.setLength(16384);
        dv.setLength(1024);
        dv.invoke();
        return dv;
    }
    
    public Trimaran getTrimaran() {
        return trimaran;
    }

    private void setTrimaran(Trimaran trimaran) {
        this.trimaran = trimaran;
    }

}
