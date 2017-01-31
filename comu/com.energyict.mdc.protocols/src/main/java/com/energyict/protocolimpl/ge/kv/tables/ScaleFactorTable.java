/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ScaleFactorTable.java
 *
 * Created on 14 november 2005, 11:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ScaleFactorTable extends AbstractTable {

    private int voltSquareScaleFactor;
    private int currentSquareScaleFactor;
    private int demandScaleFactorVA;
    private int energyScaleFactorVA;

    /** Creates a new instance of ScaleFactorTable */
    public ScaleFactorTable(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(75,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ScaleFactorTable: voltSquareScaleFactor="+getVoltSquareScaleFactor()+", currentSquareScaleFactor="+getCurrentSquareScaleFactor()+", demandScaleFactorVA="+getDemandScaleFactorVA()+", energyScaleFactorVA="+getEnergyScaleFactorVA()+"\n");

        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset=0;
        setVoltSquareScaleFactor(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setCurrentSquareScaleFactor(C12ParseUtils.getInt(tableData,offset,3,dataOrder));
        offset+=3;
        setDemandScaleFactorVA(C12ParseUtils.getInt(tableData,offset,3,dataOrder));
        offset+=3;
        setEnergyScaleFactorVA(C12ParseUtils.getInt(tableData,offset,2, dataOrder));
        offset+=2;
    }

    public int getVoltSquareScaleFactor() {
        return voltSquareScaleFactor;
    }

    public void setVoltSquareScaleFactor(int voltSquareScaleFactor) {
        this.voltSquareScaleFactor = voltSquareScaleFactor;
    }

    public int getCurrentSquareScaleFactor() {
        return currentSquareScaleFactor;
    }

    public void setCurrentSquareScaleFactor(int currentSquareScaleFactor) {
        this.currentSquareScaleFactor = currentSquareScaleFactor;
    }

    public int getDemandScaleFactorVA() {
        return demandScaleFactorVA;
    }

    public void setDemandScaleFactorVA(int demandScaleFactorVA) {
        this.demandScaleFactorVA = demandScaleFactorVA;
    }

    public int getEnergyScaleFactorVA() {
        return energyScaleFactorVA;
    }

    public void setEnergyScaleFactorVA(int energyScaleFactorVA) {
        this.energyScaleFactorVA = energyScaleFactorVA;
    }
}
