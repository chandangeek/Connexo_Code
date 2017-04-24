/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ConfigurationTable.java
 *
 * Created on 18 oktober 2005, 16:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class ConfigurationTable extends AbstractTable {

    // format control 1
    private int dataOrder;
    private int charFormat;
    private int modelSelect;
    // format control 2
    private int timeFormat;
    private int dataAccessMethod;
    private int idForm; // 1 = BCD 0 = Char
    private int intFormat;
    // format control 3
    private int nonIntFormat1;
    private int nonIntFormat2;
    // general config
    private String manufacturer;
    private int nameplateType;
    private int defaultSetUsed;
    private int maxProcParamLength;
    private int maxResponseDataLength;
    private int standardVersionNr;
    private int standardRevisionNr;
    private int dimStdTablesUsed;
    private int dimMfgTablesUsed;
    private int dimStdProcUsed;
    private int dimMfgProcUsed;
    private int dimMfgStatusUsed;
    private int nrPending;
    private byte[] stdTablesUsed;
    private byte[] mfgTablesUsed;
    private byte[] stdProcUsed;
    private byte[] mfgProcUsed;
    private byte[] stdTablesWrite;
    private byte[] mfgTablesWrite;


    /** Creates a new instance of ConfigurationTable */
    public ConfigurationTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(0));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();

        strBuff.append("ConfigurationTable:\n");
        strBuff.append("    format control 1: ");
        strBuff.append("dataOrder="+getDataOrder()+", ");
        strBuff.append("charFormat="+getCharFormat()+", ");
        strBuff.append("modelSelect="+getModelSelect()+"\n");
        strBuff.append("    format control 2: ");
        strBuff.append("timeFormat="+getTimeFormat()+", ");
        strBuff.append("dataAccessMethod="+getDataAccessMethod()+", ");
        strBuff.append("idForm="+getIdForm()+", ");
        strBuff.append("intFormat="+getIntFormat()+"\n");
        strBuff.append("    format control 3: ");
        strBuff.append("nonIntFormat1="+getNonIntFormat1()+", ");
        strBuff.append("nonIntFormat2="+getNonIntFormat2()+"\n");
        strBuff.append("    general config: ");
        strBuff.append("manufacturer="+getManufacturer()+", ");
        strBuff.append("nameplateType="+getNameplateType()+", ");
        strBuff.append("defaultSetUsed="+getDefaultSetUsed()+", ");
        strBuff.append("maxProcParamLength="+getMaxProcParamLength()+", ");
        strBuff.append("maxResponseDataLength="+getMaxResponseDataLength()+", ");
        strBuff.append("standardVersionNr="+getStandardVersionNr()+"\n");
        strBuff.append("    standardRevisionNr="+getStandardRevisionNr()+", ");
        strBuff.append("dimStdTablesUsed="+getDimStdTablesUsed()+", ");
        strBuff.append("dimMfgTablesUsed="+getDimMfgTablesUsed()+", ");
        strBuff.append("dimStdProcUsed="+getDimStdProcUsed()+", ");
        strBuff.append("dimMfgProcUsed="+getDimMfgProcUsed()+", ");
        strBuff.append("dimMfgStatusUsed="+getDimMfgStatusUsed()+", ");
        strBuff.append("nrPending="+getNrPending()+"\n");
        strBuff.append("    stdTablesUsed="+ProtocolUtils.getResponseData(getStdTablesUsed())+", "+listBitsUsed(getStdTablesUsed())+"\n");
        strBuff.append("    mfgTablesUsed="+ProtocolUtils.getResponseData(getMfgTablesUsed())+", "+listBitsUsed(getMfgTablesUsed())+"\n");
        strBuff.append("    stdProcUsed="+ProtocolUtils.getResponseData(getStdProcUsed())+", "+listBitsUsed(getStdProcUsed())+"\n");
        strBuff.append("    mfgProcUsed="+ProtocolUtils.getResponseData(getMfgProcUsed())+", "+listBitsUsed(getMfgProcUsed())+"\n");
        strBuff.append("    stdTablesWrite="+ProtocolUtils.getResponseData(getStdTablesWrite())+", "+listBitsUsed(getStdTablesWrite())+"\n");
        strBuff.append("    mfgTablesWrite="+ProtocolUtils.getResponseData(getMfgTablesWrite())+", "+listBitsUsed(getMfgTablesWrite())+"\n");
        return strBuff.toString();
    }


    private String listBitsUsed(byte[] data) {
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<data.length*8;i++) {
            if ((data[i/8] & (1<<(i%8))) == (1<<(i%8)))
                strBuff.append(i+" ");
        }
        return strBuff.toString();
    }

    public boolean isStdTableUsed(int tableId) {
        byte[] data = getStdTablesUsed();
        for (int i=0;i<data.length*8;i++) {
            if ((data[i/8] & (1<<(i%8))) == (1<<(i%8)))
                if (tableId == i)
                    return true;
        }
        return false;
    }

    protected void parse(byte[] tableData) throws IOException {
        int temp;
        // format control 1
        temp = C12ParseUtils.getInt(tableData,0);
        setDataOrder(temp&0x01);
        setCharFormat((temp&0x0E)>>1);
        setModelSelect((temp&0x70)>>4);
        // format control 2
        temp = C12ParseUtils.getInt(tableData,1);
        setTimeFormat(temp&0x07);
        setDataAccessMethod((temp&0x18)>>3);
        setIdForm((temp&0x20)>>5);
        setIntFormat((temp&0xC0)>>6);
        // format control 3
        temp = C12ParseUtils.getInt(tableData,2);
        setNonIntFormat1(temp&0x0F);
        setNonIntFormat2((temp&0xF0)>>4);
        // general config
        setManufacturer(new String(ProtocolUtils.getSubArray2(tableData,3, 4)));
        setNameplateType(C12ParseUtils.getInt(tableData,7));
        setDefaultSetUsed(C12ParseUtils.getInt(tableData,8));
        setMaxProcParamLength(C12ParseUtils.getInt(tableData,9));
        setMaxResponseDataLength(C12ParseUtils.getInt(tableData,10));
        setStandardVersionNr(C12ParseUtils.getInt(tableData,11));
        setStandardRevisionNr(C12ParseUtils.getInt(tableData,12));
        setDimStdTablesUsed(C12ParseUtils.getInt(tableData,13));
        setDimMfgTablesUsed(C12ParseUtils.getInt(tableData,14));
        setDimStdProcUsed(C12ParseUtils.getInt(tableData,15));
        setDimMfgProcUsed(C12ParseUtils.getInt(tableData,16));
        setDimMfgStatusUsed(C12ParseUtils.getInt(tableData,17));
        setNrPending(C12ParseUtils.getInt(tableData,18));
        int offset = 19;
        setStdTablesUsed(ProtocolUtils.getSubArray2(tableData, offset, getDimStdTablesUsed()));
        offset+=getDimStdTablesUsed();
        setMfgTablesUsed(ProtocolUtils.getSubArray2(tableData, offset, getDimMfgTablesUsed()));
        offset+=getDimMfgTablesUsed();
        setStdProcUsed(ProtocolUtils.getSubArray2(tableData, offset, getDimStdProcUsed()));
        offset+=getDimStdProcUsed();
        setMfgProcUsed(ProtocolUtils.getSubArray2(tableData, offset, getDimMfgProcUsed()));
        offset+=getDimMfgProcUsed();
        setStdTablesWrite(ProtocolUtils.getSubArray2(tableData, offset, getDimStdTablesUsed()));
        offset+=getDimStdTablesUsed();
        setMfgTablesWrite(ProtocolUtils.getSubArray2(tableData, offset, getDimMfgTablesUsed()));

    }

    public int getDataOrder() {
        return dataOrder;
    }

    public void setDataOrder(int dataOrder) {
        this.dataOrder = dataOrder;
    }

    public int getCharFormat() {
        return charFormat;
    }

    public void setCharFormat(int charFormat) {
        this.charFormat = charFormat;
    }

    public int getModelSelect() {
        return modelSelect;
    }

    public void setModelSelect(int modelSelect) {
        this.modelSelect = modelSelect;
    }

    public int getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(int timeFormat) {
        this.timeFormat = timeFormat;
    }

    public int getDataAccessMethod() {
        return dataAccessMethod;
    }

    public void setDataAccessMethod(int dataAccessMethod) {
        this.dataAccessMethod = dataAccessMethod;
    }

    public int getIdForm() {
        return idForm;
    }

    public void setIdForm(int idForm) {
        this.idForm = idForm;
    }

    public int getIntFormat() {
        return intFormat;
    }

    public void setIntFormat(int intFormat) {
        this.intFormat = intFormat;
    }

    public int getNonIntFormat1() {
        return nonIntFormat1;
    }

    public void setNonIntFormat1(int nonIntFormat1) {
        this.nonIntFormat1 = nonIntFormat1;
    }

    public int getNonIntFormat2() {
        return nonIntFormat2;
    }

    public void setNonIntFormat2(int nonIntFormat2) {
        this.nonIntFormat2 = nonIntFormat2;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getNameplateType() {
        return nameplateType;
    }

    public void setNameplateType(int nameplateType) {
        this.nameplateType = nameplateType;
    }

    public int getDefaultSetUsed() {
        return defaultSetUsed;
    }

    public void setDefaultSetUsed(int defaultSetUsed) {
        this.defaultSetUsed = defaultSetUsed;
    }

    public int getMaxProcParamLength() {
        return maxProcParamLength;
    }

    public void setMaxProcParamLength(int maxProcParamLength) {
        this.maxProcParamLength = maxProcParamLength;
    }

    public int getMaxResponseDataLength() {
        return maxResponseDataLength;
    }

    public void setMaxResponseDataLength(int maxResponseDataLength) {
        this.maxResponseDataLength = maxResponseDataLength;
    }

    public int getStandardVersionNr() {
        return standardVersionNr;
    }

    public void setStandardVersionNr(int standardVersionNr) {
        this.standardVersionNr = standardVersionNr;
    }

    public int getStandardRevisionNr() {
        return standardRevisionNr;
    }

    public void setStandardRevisionNr(int standardRevisionNr) {
        this.standardRevisionNr = standardRevisionNr;
    }

    public int getDimStdTablesUsed() {
        return dimStdTablesUsed;
    }

    public void setDimStdTablesUsed(int dimStdTablesUsed) {
        this.dimStdTablesUsed = dimStdTablesUsed;
    }

    public int getDimMfgTablesUsed() {
        return dimMfgTablesUsed;
    }

    public void setDimMfgTablesUsed(int dimMfgTablesUsed) {
        this.dimMfgTablesUsed = dimMfgTablesUsed;
    }

    public int getDimStdProcUsed() {
        return dimStdProcUsed;
    }

    public void setDimStdProcUsed(int dimStdProcUsed) {
        this.dimStdProcUsed = dimStdProcUsed;
    }

    public int getDimMfgProcUsed() {
        return dimMfgProcUsed;
    }

    public void setDimMfgProcUsed(int dimMfgProcUsed) {
        this.dimMfgProcUsed = dimMfgProcUsed;
    }

    public int getDimMfgStatusUsed() {
        return dimMfgStatusUsed;
    }

    public void setDimMfgStatusUsed(int dimMfgStatusUsed) {
        this.dimMfgStatusUsed = dimMfgStatusUsed;
    }

    public int getNrPending() {
        return nrPending;
    }

    public void setNrPending(int nrPending) {
        this.nrPending = nrPending;
    }

    public byte[] getStdTablesUsed() {
        return stdTablesUsed;
    }

    public void setStdTablesUsed(byte[] stdTablesUsed) {
        this.stdTablesUsed = stdTablesUsed;
    }

    public byte[] getMfgTablesUsed() {
        return mfgTablesUsed;
    }

    public void setMfgTablesUsed(byte[] mfgTablesUsed) {
        this.mfgTablesUsed = mfgTablesUsed;
    }

    public byte[] getStdProcUsed() {
        return stdProcUsed;
    }

    public void setStdProcUsed(byte[] stdProcUsed) {
        this.stdProcUsed = stdProcUsed;
    }

    public byte[] getMfgProcUsed() {
        return mfgProcUsed;
    }

    public void setMfgProcUsed(byte[] mfgProcUsed) {
        this.mfgProcUsed = mfgProcUsed;
    }

    public byte[] getStdTablesWrite() {
        return stdTablesWrite;
    }

    public void setStdTablesWrite(byte[] stdTablesWrite) {
        this.stdTablesWrite = stdTablesWrite;
    }

    public byte[] getMfgTablesWrite() {
        return mfgTablesWrite;
    }

    public void setMfgTablesWrite(byte[] mfgTablesWrite) {
        this.mfgTablesWrite = mfgTablesWrite;
    }
}
