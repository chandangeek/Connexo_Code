/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DisplayConfigurationTable.java
 *
 * Created on 14 november 2005, 9:16
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
public class DisplayConfigurationTable extends AbstractTable {

    private int dateFormat; // 1 byte
    private int suppressLeadZeros; // 1 byte
    private int dispScalar; // 1 byte
    private int demandDispUnits; // 1 byte
    private int primaryDisplay; // 1 byte
    private long dispMultiplier;  // 4 byte
    private int cumDemandDigits; // 1 byte
    private int demandDigits; // 1 byte
    private int energyDigits; // 1 byte
    private int lineLineVoltageDisplayEnabled; // 1 byte


    /** Creates a new instance of DisplayConfigurationTable */
    public DisplayConfigurationTable(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(70,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DisplayConfigurationTable: dateFormat="+getDateFormat()+
                       ", suppressLeadZeros="+getSuppressLeadZeros()+
                       ", dispScalar="+getDispScalar()+
                       ", demandDispUnits="+getDemandDispUnits()+
                       ", primaryDisplay="+getPrimaryDisplay()+
                       ", dispMultiplier="+getDispMultiplier()+
                       ", cumDemandDigits="+getCumDemandDigits()+
                       ", demandDigits="+getDemandDigits()+
                       ", energyDigits="+getEnergyDigits()+
                       ", lineLineVoltageDisplayEnabled="+getLineLineVoltageDisplayEnabled()+"\n");

        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setDateFormat(C12ParseUtils.getInt(tableData,offset++));
        setSuppressLeadZeros(C12ParseUtils.getInt(tableData,offset++));
        setDispScalar(C12ParseUtils.getInt(tableData,offset++));
        setDemandDispUnits(C12ParseUtils.getInt(tableData,offset++));
        setPrimaryDisplay(C12ParseUtils.getInt(tableData,offset++));
        setDispMultiplier(C12ParseUtils.getLong(tableData,offset,4, dataOrder));
        offset+=4;
        setCumDemandDigits(C12ParseUtils.getInt(tableData,offset++));
        setDemandDigits(C12ParseUtils.getInt(tableData,offset++));
        setEnergyDigits(C12ParseUtils.getInt(tableData,offset++));
        setLineLineVoltageDisplayEnabled(C12ParseUtils.getInt(tableData,offset++));

    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    public int getSuppressLeadZeros() {
        return suppressLeadZeros;
    }

    public void setSuppressLeadZeros(int suppressLeadZeros) {
        this.suppressLeadZeros = suppressLeadZeros;
    }

    public int getDispScalar() {
        return dispScalar;
    }

    public void setDispScalar(int dispScalar) {
        this.dispScalar = dispScalar;
    }

    public int getDemandDispUnits() {
        return demandDispUnits;
    }

    public void setDemandDispUnits(int demandDispUnits) {
        this.demandDispUnits = demandDispUnits;
    }

    public int getPrimaryDisplay() {
        return primaryDisplay;
    }

    public void setPrimaryDisplay(int primaryDisplay) {
        this.primaryDisplay = primaryDisplay;
    }

    public long getDispMultiplier() {
        return dispMultiplier;
    }

    public void setDispMultiplier(long dispMultiplier) {
        this.dispMultiplier = dispMultiplier;
    }

    public int getCumDemandDigits() {
        return cumDemandDigits;
    }

    public void setCumDemandDigits(int cumDemandDigits) {
        this.cumDemandDigits = cumDemandDigits;
    }

    public int getDemandDigits() {
        return demandDigits;
    }

    public void setDemandDigits(int demandDigits) {
        this.demandDigits = demandDigits;
    }

    public int getEnergyDigits() {
        return energyDigits;
    }

    public void setEnergyDigits(int energyDigits) {
        this.energyDigits = energyDigits;
    }

    public int getLineLineVoltageDisplayEnabled() {
        return lineLineVoltageDisplayEnabled;
    }

    public void setLineLineVoltageDisplayEnabled(int lineLineVoltageDisplayEnabled) {
        this.lineLineVoltageDisplayEnabled = lineLineVoltageDisplayEnabled;
    }

}
