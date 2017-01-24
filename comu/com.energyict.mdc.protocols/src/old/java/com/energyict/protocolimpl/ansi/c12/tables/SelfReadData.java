/*
 * SelfReadData.java
 *
 * Created on 28 oktober 2005, 16:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SelfReadData {

    private int selfReadSeqNr; // 16 bit
    private RegisterInf registerInfo;
    private RegisterData selfReadRegisterData;

    /** Creates a new instance of SelfReadData */
    public SelfReadData(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (art.isSelfReadSeqNrFlag()) {
           setSelfReadSeqNr(C12ParseUtils.getInt(data,offset,2,dataOrder));
           offset+=2;
        }
        setRegisterInfo(new RegisterInf(data,offset,tableFactory));
        offset+=RegisterInf.getSize(tableFactory);
        setSelfReadRegisterData(new RegisterData(data,offset,tableFactory));
        offset+=RegisterData.getSize(tableFactory);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadData: \n");
        strBuff.append("selfReadSeqNr="+getSelfReadSeqNr()+"\n");
        strBuff.append("registerInfo="+getRegisterInfo()+"\n");
        strBuff.append("selfReadRegisterData="+getSelfReadRegisterData()+"\n");
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int size=0;
        if (art.isSelfReadSeqNrFlag()) size+=2;
        size+=RegisterInf.getSize(tableFactory);
        size+=RegisterData.getSize(tableFactory);
        return size;
    }

    public int getSelfReadSeqNr() {
        return selfReadSeqNr;
    }

    public void setSelfReadSeqNr(int selfReadSeqNr) {
        this.selfReadSeqNr = selfReadSeqNr;
    }

    public RegisterInf getRegisterInfo() {
        return registerInfo;
    }

    public void setRegisterInfo(RegisterInf registerInfo) {
        this.registerInfo = registerInfo;
    }

    public RegisterData getSelfReadRegisterData() {
        return selfReadRegisterData;
    }

    public void setSelfReadRegisterData(RegisterData selfReadRegisterData) {
        this.selfReadRegisterData = selfReadRegisterData;
    }
}
