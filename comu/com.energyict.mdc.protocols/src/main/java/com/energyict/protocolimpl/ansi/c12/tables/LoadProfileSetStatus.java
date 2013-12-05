/*
 * LoadProfileSetStatus.java
 *
 * Created on 8 november 2005, 11:12
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
public class LoadProfileSetStatus {

    private int loadProfileSetStatusflags; // 8 bit
    private int blockOrder; // bit 0  0=ascending order, 1=descending order
    private boolean overflowFlag; //bit 1
    private int listType;  // bit 2 0=FIFO, 1=circular
    private boolean blockInhibitOverflowFlag; // bit 3 if true, inhibit load profile when an overflow occurs
    private int intervalOrder; // bit 4  0=ascending order, 1=descending order
    private boolean activeModeFlag; // bit 5
    private int testMode; // bit 6

    private int nrOfValidBlocks; // 16 bit number of valid load profile data blocks in load profile data tables
    private int lastBlockElement; // 16 bit the array element of the newest valid data block in the load profile data array
    private long lastBlockSequenceNumber; // 32 bit the sequence number of the last element in the load profile data array
    private int nrOfUnreadBlocks; // 16 bit the number of load profile data blocks that not have been read
    private int nrOfValidIntervals; // 16 bit nr of valid intervals stored in the last load profile block array. The range is 0 to actual dimension of number of intervals per block


    /** Creates a new instance of LoadProfileSetStatus */
    public LoadProfileSetStatus(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setLoadProfileSetStatusflags(C12ParseUtils.getInt(data,offset++));
        setBlockOrder(getLoadProfileSetStatusflags() & 0x01);
        setOverflowFlag((getLoadProfileSetStatusflags() & 0x02) == 0x02);
        setListType((getLoadProfileSetStatusflags() & 0x04) >> 2);
        setBlockInhibitOverflowFlag((getLoadProfileSetStatusflags() & 0x08) == 0x08);
        setIntervalOrder((getLoadProfileSetStatusflags() & 0x10) >> 4);
        setActiveModeFlag((getLoadProfileSetStatusflags() & 0x20) == 0x20);
        setTestMode((getLoadProfileSetStatusflags() & 0x40) >> 6);

        setNrOfValidBlocks(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;
        setLastBlockElement(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;
        setLastBlockSequenceNumber(C12ParseUtils.getLong(data,offset, 4, dataOrder));
        offset+=4;
        setNrOfUnreadBlocks(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;
        setNrOfValidIntervals(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileSetStatus: \n");
        strBuff.append("    loadProfileSetStatusflags=0x"+Integer.toHexString(loadProfileSetStatusflags)+"\n");
        strBuff.append("    nrOfValidBlocks="+nrOfValidBlocks+"\n");
        strBuff.append("    lastBlockElement="+lastBlockElement+"\n");
        strBuff.append("    lastBlockSequenceNumber="+lastBlockSequenceNumber+"\n");
        strBuff.append("    nrOfUnreadBlocks="+nrOfUnreadBlocks+"\n");
        strBuff.append("    nrOfValidIntervals="+nrOfValidIntervals+"\n");
        strBuff.append("    intervalOrder="+intervalOrder+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 13;
    }

    public int getLoadProfileSetStatusflags() {
        return loadProfileSetStatusflags;
    }

    public void setLoadProfileSetStatusflags(int loadProfileSetStatusflags) {
        this.loadProfileSetStatusflags = loadProfileSetStatusflags;
    }

    public int getBlockOrder() {
        return blockOrder;
    }

    public void setBlockOrder(int blockOrder) {
        this.blockOrder = blockOrder;
    }

    public boolean isOverflowFlag() {
        return overflowFlag;
    }

    public void setOverflowFlag(boolean overflowFlag) {
        this.overflowFlag = overflowFlag;
    }

    public int getListType() {
        return listType;
    }

    public void setListType(int listType) {
        this.listType = listType;
    }

    public boolean isBlockInhibitOverflowFlag() {
        return blockInhibitOverflowFlag;
    }

    public void setBlockInhibitOverflowFlag(boolean blockInhibitOverflowFlag) {
        this.blockInhibitOverflowFlag = blockInhibitOverflowFlag;
    }

    public int getIntervalOrder() {
        return intervalOrder;
    }

    public void setIntervalOrder(int intervalOrder) {
        this.intervalOrder = intervalOrder;
    }

    public boolean isActiveModeFlag() {
        return activeModeFlag;
    }

    public void setActiveModeFlag(boolean activeModeFlag) {
        this.activeModeFlag = activeModeFlag;
    }

    public int getTestMode() {
        return testMode;
    }

    public void setTestMode(int testMode) {
        this.testMode = testMode;
    }

    public int getNrOfValidBlocks() {
        return nrOfValidBlocks;
    }

    public void setNrOfValidBlocks(int nrOfValidBlocks) {
        this.nrOfValidBlocks = nrOfValidBlocks;
    }

    public int getLastBlockElement() {
        return lastBlockElement;
    }

    public void setLastBlockElement(int lastBlockElement) {
        this.lastBlockElement = lastBlockElement;
    }

    public long getLastBlockSequenceNumber() {
        return lastBlockSequenceNumber;
    }

    public void setLastBlockSequenceNumber(long lastBlockSequenceNumber) {
        this.lastBlockSequenceNumber = lastBlockSequenceNumber;
    }

    public int getNrOfUnreadBlocks() {
        return nrOfUnreadBlocks;
    }

    public void setNrOfUnreadBlocks(int nrOfUnreadBlocks) {
        this.nrOfUnreadBlocks = nrOfUnreadBlocks;
    }

    public int getNrOfValidIntervals() {
        return nrOfValidIntervals;
    }

    public void setNrOfValidIntervals(int nrOfValidIntervals) {
        this.nrOfValidIntervals = nrOfValidIntervals;
    }
}
