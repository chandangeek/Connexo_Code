/*
 * ScaleFactorTable.java
 *
 * Created on 14 november 2005, 11:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv2.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ScaleFactorTable extends AbstractTable {

    private int voltSquareLine2NeutralScaleFactor; // 2 bytes
    private int voltSquareLine2LineScaleFactor; // 2 bytes
    private int currentSquareScaleFactor; // 3 bytes
    private int neutralCurrentSquareScaleFactor; // 3 bytes
    private int demandScaleFactorVA; // 3 bytes
    private int energyScaleFactorVA; // 2 bytes



    /** Creates a new instance of ScaleFactorTable */
    public ScaleFactorTable(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(75,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ScaleFactorTable: voltSquareLine2NeutralScaleFactor="+getVoltSquareLine2NeutralScaleFactor()+", voltSquareLine2LineScaleFactor="+getVoltSquareLine2LineScaleFactor()+", currentSquareScaleFactor="+getCurrentSquareScaleFactor()+", neutralCurrentSquareScaleFactor="+getNeutralCurrentSquareScaleFactor()+", demandScaleFactorVA="+getDemandScaleFactorVA()+", energyScaleFactorVA="+getEnergyScaleFactorVA()+"\n");

        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset=0;
        setVoltSquareLine2NeutralScaleFactor(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setVoltSquareLine2LineScaleFactor(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setCurrentSquareScaleFactor(C12ParseUtils.getInt(tableData,offset,3,dataOrder));
        offset+=3;
        setNeutralCurrentSquareScaleFactor(C12ParseUtils.getInt(tableData,offset,3,dataOrder));
        offset+=3;
        setDemandScaleFactorVA(C12ParseUtils.getInt(tableData,offset,3,dataOrder));
        offset+=3;
        setEnergyScaleFactorVA(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
    }

    public int getVoltSquareLine2NeutralScaleFactor() {
        return voltSquareLine2NeutralScaleFactor;
    }

    public void setVoltSquareLine2NeutralScaleFactor(int voltSquareLine2NeutralScaleFactor) {
        this.voltSquareLine2NeutralScaleFactor = voltSquareLine2NeutralScaleFactor;
    }

    public int getVoltSquareLine2LineScaleFactor() {
        return voltSquareLine2LineScaleFactor;
    }

    public void setVoltSquareLine2LineScaleFactor(int voltSquareLine2LineScaleFactor) {
        this.voltSquareLine2LineScaleFactor = voltSquareLine2LineScaleFactor;
    }

    public int getCurrentSquareScaleFactor() {
        return currentSquareScaleFactor;
    }

    public void setCurrentSquareScaleFactor(int currentSquareScaleFactor) {
        this.currentSquareScaleFactor = currentSquareScaleFactor;
    }

    public int getNeutralCurrentSquareScaleFactor() {
        return neutralCurrentSquareScaleFactor;
    }

    public void setNeutralCurrentSquareScaleFactor(int neutralCurrentSquareScaleFactor) {
        this.neutralCurrentSquareScaleFactor = neutralCurrentSquareScaleFactor;
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
