/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterProgramConstants1.java
 *
 * Created on 10 november 2005, 14:03
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
public class MeterProgramConstants1 extends AbstractTable {

    private int frequency; // 8 bit, 0=50Hz, 1=60Hz
    private int timeBase; // 8 bit, 0=line, 1=crystal
    private long kt; // 4 bytes
    private int driftCompensationInternal; // 3 bytes
    private int driftCompensationSign; // 1 byte
    private int phaseAngle; // 3 bits
    private int outputtype; // 1 bit
    private int sampleCase; // 4 bits
    private int voltSampleScalar; // 2 bytes
    private int voltSampleDivisor; // 2 bytes
    private int currSampleScalar; // 2 bytes
    private int currSampleDivisor; // 2 bytes
    //reserved 4 bytes

    /** Creates a new instance of MeterProgramConstants1 */
    public MeterProgramConstants1(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(66,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterProgramConstants1:\n");
        strBuff.append("    frequency="+getFrequency()+", timeBase="+getTimeBase()+", kt="+getKt()+"\n");
        strBuff.append("    driftCompensationInternal="+driftCompensationInternal+", ");
        strBuff.append("driftCompensationSign="+driftCompensationSign+", ");
        strBuff.append("phaseAngle="+phaseAngle+", ");
        strBuff.append("outputtype="+outputtype+", ");
        strBuff.append("sampleCase="+sampleCase+"\n");
        strBuff.append("    voltSampleScalar="+voltSampleScalar+", ");
        strBuff.append("voltSampleDivisor="+voltSampleDivisor+", ");
        strBuff.append("currSampleScalar="+currSampleScalar+", ");
        strBuff.append("currSampleDivisor="+currSampleDivisor+"\n");
        return strBuff.toString();
    }

    protected void prepareTransfer() {

    }

    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setFrequency(C12ParseUtils.getInt(tableData,offset++));
        setTimeBase(C12ParseUtils.getInt(tableData,offset++));
        setKt(C12ParseUtils.getLong(tableData,offset,4,dataOrder));
        offset+=4;
        setDriftCompensationInternal(C12ParseUtils.getInt(tableData,offset,3,dataOrder));
        offset+=3;
        setDriftCompensationSign(C12ParseUtils.getInt(tableData,offset++));
        int temp = C12ParseUtils.getInt(tableData,offset++);
        setPhaseAngle(temp&0x07);
        setOutputtype((temp>>3)&0x01);
        setSampleCase((temp>>4)&0x0F);
        setVoltSampleScalar(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setVoltSampleDivisor(C12ParseUtils.getInt(tableData,offset,2, dataOrder));
        offset+=2;
        setCurrSampleScalar(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setCurrSampleDivisor(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;

    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(int timeBase) {
        this.timeBase = timeBase;
    }

    public long getKt() {
        return kt;
    }

    public void setKt(long kt) {
        this.kt = kt;
    }

    public int getDriftCompensationInternal() {
        return driftCompensationInternal;
    }

    public void setDriftCompensationInternal(int driftCompensationInternal) {
        this.driftCompensationInternal = driftCompensationInternal;
    }

    public int getDriftCompensationSign() {
        return driftCompensationSign;
    }

    public void setDriftCompensationSign(int driftCompensationSign) {
        this.driftCompensationSign = driftCompensationSign;
    }

    public int getPhaseAngle() {
        return phaseAngle;
    }

    public void setPhaseAngle(int phaseAngle) {
        this.phaseAngle = phaseAngle;
    }

    public int getOutputtype() {
        return outputtype;
    }

    public void setOutputtype(int outputtype) {
        this.outputtype = outputtype;
    }

    public int getSampleCase() {
        return sampleCase;
    }

    public void setSampleCase(int sampleCase) {
        this.sampleCase = sampleCase;
    }

    public int getVoltSampleScalar() {
        return voltSampleScalar;
    }

    public void setVoltSampleScalar(int voltSampleScalar) {
        this.voltSampleScalar = voltSampleScalar;
    }

    public int getVoltSampleDivisor() {
        return voltSampleDivisor;
    }

    public void setVoltSampleDivisor(int voltSampleDivisor) {
        this.voltSampleDivisor = voltSampleDivisor;
    }

    public int getCurrSampleScalar() {
        return currSampleScalar;
    }

    public void setCurrSampleScalar(int currSampleScalar) {
        this.currSampleScalar = currSampleScalar;
    }

    public int getCurrSampleDivisor() {
        return currSampleDivisor;
    }

    public void setCurrSampleDivisor(int currSampleDivisor) {
        this.currSampleDivisor = currSampleDivisor;
    }
}
