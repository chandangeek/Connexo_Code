/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterProgramConstants2.java
 *
 * Created on 10 november 2005, 14:15
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
public class MeterProgramConstants2 extends AbstractTable {
    int dummy;
    private long energyWraptestConst; // 6 bytes
    private long demandWraptestConst; // 6 bytes
    private int curTransRatio; // 2 byte
    private int potTransRatio; // 2 byte
    private int programId; // 2 byte
    private long userDefField1; // 6 bytes
    private long userDefField2; // 6 bytes
    private long userDefField3; // 6 bytes

    private int powerFailRecognitionTime; // 2 bytes
    private int loadProfileOutageDuration; // 2 bytes
    private long demandResetTimeout; // 4 bytes
    // reserved 4 bytes


    /** Creates a new instance of MeterProgramConstants2 */
    public MeterProgramConstants2(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(67,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterProgramConstants2: energyWraptestConst="+getEnergyWraptestConst()+", demandWraptestConst="+getDemandWraptestConst()+", curTransRatio="+getCurTransRatio()+", potTransRatio="+getPotTransRatio()+", programId="+getProgramId()+", userDefField1="+getUserDefField1()+", userDefField2="+getUserDefField2()+", userDefField3="+getUserDefField3()+", powerFailRecognitionTime="+getPowerFailRecognitionTime()+", loadProfileOutageDuration="+getLoadProfileOutageDuration()+", demandResetTimeout="+getDemandResetTimeout()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset=0;
        setEnergyWraptestConst(C12ParseUtils.getLong(tableData,offset,6,dataOrder));
        offset+=6;
        setDemandWraptestConst(C12ParseUtils.getLong(tableData,offset,6,dataOrder));
        offset+=6;
        setCurTransRatio(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setPotTransRatio(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setProgramId(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setUserDefField1(C12ParseUtils.getLong(tableData,offset,6,dataOrder));
        offset+=6;
        setUserDefField2(C12ParseUtils.getLong(tableData,offset,6,dataOrder));
        offset+=6;
        setUserDefField3(C12ParseUtils.getLong(tableData,offset,6,dataOrder));
        offset+=6;
        setPowerFailRecognitionTime(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setLoadProfileOutageDuration(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setDemandResetTimeout(C12ParseUtils.getLong(tableData,offset,4,dataOrder));
        offset+=4;

        // from firmware version v5.1 and greater another 2 bytes added to that table

    }

    public long getEnergyWraptestConst() {
        return energyWraptestConst;
    }

    public void setEnergyWraptestConst(long energyWraptestConst) {
        this.energyWraptestConst = energyWraptestConst;
    }

    public long getDemandWraptestConst() {
        return demandWraptestConst;
    }

    public void setDemandWraptestConst(long demandWraptestConst) {
        this.demandWraptestConst = demandWraptestConst;
    }

    public int getCurTransRatio() {
        return curTransRatio;
    }

    public void setCurTransRatio(int curTransRatio) {
        this.curTransRatio = curTransRatio;
    }

    public int getPotTransRatio() {
        return potTransRatio;
    }

    public void setPotTransRatio(int potTransRatio) {
        this.potTransRatio = potTransRatio;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public long getUserDefField1() {
        return userDefField1;
    }

    public void setUserDefField1(int userDefField1) {
        this.setUserDefField1(userDefField1);
    }

    public long getUserDefField2() {
        return userDefField2;
    }

    public void setUserDefField2(int userDefField2) {
        this.setUserDefField2(userDefField2);
    }

    public long getUserDefField3() {
        return userDefField3;
    }

    public void setUserDefField3(int userDefField3) {
        this.setUserDefField3(userDefField3);
    }

    public void setUserDefField1(long userDefField1) {
        this.userDefField1 = userDefField1;
    }

    public void setUserDefField2(long userDefField2) {
        this.userDefField2 = userDefField2;
    }

    public void setUserDefField3(long userDefField3) {
        this.userDefField3 = userDefField3;
    }

    public int getPowerFailRecognitionTime() {
        return powerFailRecognitionTime;
    }

    public void setPowerFailRecognitionTime(int powerFailRecognitionTime) {
        this.powerFailRecognitionTime = powerFailRecognitionTime;
    }

    public int getLoadProfileOutageDuration() {
        return loadProfileOutageDuration;
    }

    public void setLoadProfileOutageDuration(int loadProfileOutageDuration) {
        this.loadProfileOutageDuration = loadProfileOutageDuration;
    }

    public long getDemandResetTimeout() {
        return demandResetTimeout;
    }

    public void setDemandResetTimeout(long demandResetTimeout) {
        this.demandResetTimeout = demandResetTimeout;
    }

}
