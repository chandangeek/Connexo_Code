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

package com.energyict.protocolimpl.ge.kv2.tables;

import com.energyict.protocols.util.ProtocolUtils;

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
    int fixedPointFormat1; // 1 byte
    int fixedPointFormat2; // 1 byte
    int numericFormat1; // 1 byte
    int numericFormat2; // 1 byte
    String[] userDefinedLabels; // 5 x 6 byte

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
                       ", fixedPointFormat1="+fixedPointFormat1+
                       ", fixedPointFormat2="+fixedPointFormat2+
                       ", numericFormat1="+numericFormat1+
                       ", numericFormat2="+numericFormat2+"\n");
        for (int i=0;i<5;i++) {
            strBuff.append("userDefinedLabels["+i+"]="+userDefinedLabels[i]+"\n");
        }

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
        fixedPointFormat1 = C12ParseUtils.getInt(tableData,offset++);
        fixedPointFormat2 = C12ParseUtils.getInt(tableData,offset++);
        numericFormat1 = C12ParseUtils.getInt(tableData,offset++);
        numericFormat2 = C12ParseUtils.getInt(tableData,offset++);
        userDefinedLabels = new String[5];
        for (int i=0;i<5;i++) {
           userDefinedLabels[i] = new String(ProtocolUtils.getSubArray2(tableData,offset, 6));
           offset+=6;
        }

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

}
