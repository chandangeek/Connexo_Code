/*
 * SelfReadList.java
 *
 * Created on 28 oktober 2005, 16:18
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
public class SelfReadList {

    private int listStatusBitfield; // 8 bit
    private int nrOfValidEntries; // 8 bit
    private int lastEntryElement; // 8 bit
    private int lastEntrySeqNr; // 16 bit
    private int nrOfUnrteadEntries; // 8
    private SelfReadData[] selfReadEntries;


    /** Creates a new instance of SelfReadList */
    public SelfReadList(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setListStatusBitfield(C12ParseUtils.getInt(data,offset));
        offset++;
        setNrOfValidEntries(C12ParseUtils.getInt(data,offset));
        offset++;
        setLastEntryElement(C12ParseUtils.getInt(data,offset));
        offset++;
        setLastEntrySeqNr(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;
        setNrOfUnrteadEntries(C12ParseUtils.getInt(data,offset));
        offset++;
        setSelfReadEntries(new SelfReadData[art.getNrOfSelfReads()]);
        for (int i=0;i<getSelfReadEntries().length;i++) {
            getSelfReadEntries()[i] = new SelfReadData(data,offset,tableFactory);
            offset+=SelfReadData.getSize(tableFactory);
        }
    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadList: \n");
        strBuff.append("    listStatusBitfield=0x"+Integer.toHexString(getListStatusBitfield())+"\n");
        strBuff.append("    nrOfValidEntries="+getNrOfValidEntries()+"\n");
        strBuff.append("    lastEntryElement="+getLastEntryElement()+"\n");
        strBuff.append("    lastEntrySeqNr="+getLastEntrySeqNr()+"\n");
        strBuff.append("    nrOfUnrteadEntries="+getNrOfUnrteadEntries()+"\n");
        for (int i=0;i<getSelfReadEntries().length;i++)
            strBuff.append("    selfReadEntries["+i+"]="+getSelfReadEntries()[i]+"\n");

        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 6+art.getNrOfSelfReads()*SelfReadData.getSize(tableFactory);
    }

    public int getListStatusBitfield() {
        return listStatusBitfield;
    }

    public void setListStatusBitfield(int listStatusBitfield) {
        this.listStatusBitfield = listStatusBitfield;
    }

    public int getNrOfValidEntries() {
        return nrOfValidEntries;
    }

    public void setNrOfValidEntries(int nrOfValidEntries) {
        this.nrOfValidEntries = nrOfValidEntries;
    }

    public int getLastEntryElement() {
        return lastEntryElement;
    }

    public void setLastEntryElement(int lastEntryElement) {
        this.lastEntryElement = lastEntryElement;
    }

    public int getLastEntrySeqNr() {
        return lastEntrySeqNr;
    }

    public void setLastEntrySeqNr(int lastEntrySeqNr) {
        this.lastEntrySeqNr = lastEntrySeqNr;
    }

    public int getNrOfUnrteadEntries() {
        return nrOfUnrteadEntries;
    }

    public void setNrOfUnrteadEntries(int nrOfUnrteadEntries) {
        this.nrOfUnrteadEntries = nrOfUnrteadEntries;
    }

    public SelfReadData[] getSelfReadEntries() {
        return selfReadEntries;
    }

    public void setSelfReadEntries(SelfReadData[] selfReadEntries) {
        this.selfReadEntries = selfReadEntries;
    }
}
