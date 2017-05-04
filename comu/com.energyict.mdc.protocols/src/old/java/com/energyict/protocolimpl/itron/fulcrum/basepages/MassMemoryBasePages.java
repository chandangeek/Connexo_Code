/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MassMemoryBasePages.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MassMemoryBasePages extends AbstractBasePage {

    final int MAX_NR_OF_CHANNELS=8;
    private MassMemoryProgramTable[] massMemoryProgramTable;
    private int massMemoryRecordLength; // 2 bytes
    private int logicalMassMemoryStartAddress; // 3 bytes
    private int logicalMassMemoryEndAddress; // 3 bytes
    private int actualMassMemoryEndAddress; // 3 bytes
    private int currentLogicalMassMemoryAddress; // 3 bytes
    // reserved 3 bytes
    private int currentMassMemoryRecordNumber; // 2 bytes
    private int currentMassMemoryIntervalNumber; // 1 bytes
    private int numberOfChannels; // 1 byte
    // reserved 1 byte
    private int recordingIntervalLength; // 1 byte in minutes
    private int massMemoryOutageLength; // 1 byte



    /** Creates a new instance of MassMemoryBasePages */
    public MassMemoryBasePages(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryBasePages:\n");
        strBuff.append("   actualMassMemoryEndAddress=0x"+Integer.toHexString(getActualMassMemoryEndAddress())+"\n");
        strBuff.append("   currentLogicalMassMemoryAddress=0x"+Integer.toHexString(getCurrentLogicalMassMemoryAddress())+"\n");
        strBuff.append("   currentMassMemoryIntervalNumber="+getCurrentMassMemoryIntervalNumber()+"\n");
        strBuff.append("   currentMassMemoryRecordNumber="+getCurrentMassMemoryRecordNumber()+"\n");
        strBuff.append("   logicalMassMemoryEndAddress==0x"+Integer.toHexString(getLogicalMassMemoryEndAddress())+"\n");
        strBuff.append("   logicalMassMemoryStartAddress==0x"+Integer.toHexString(getLogicalMassMemoryStartAddress())+"\n");
        strBuff.append("   massMemoryOutageLength="+getMassMemoryOutageLength()+"\n");
        for (int i=0;i<getMassMemoryProgramTable().length;i++) {
            strBuff.append("       massMemoryProgramTable["+i+"]="+getMassMemoryProgramTable()[i]+"\n");
        }
        strBuff.append("   massMemoryRecordLength="+getMassMemoryRecordLength()+"\n");
        strBuff.append("   numberOfChannels="+getNumberOfChannels()+"\n");
        strBuff.append("   recordingIntervalLength="+getRecordingIntervalLength()+"\n");
        return strBuff.toString();
    }


    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x32C9,0x3361-0x32C9);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setMassMemoryProgramTable(new MassMemoryProgramTable[MAX_NR_OF_CHANNELS]);
        for(int i=0;i<MAX_NR_OF_CHANNELS;i++) {
            getMassMemoryProgramTable()[i] = new MassMemoryProgramTable(data,offset);
            offset+=MassMemoryProgramTable.size();
        }

        setMassMemoryRecordLength(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setLogicalMassMemoryStartAddress(ProtocolUtils.getInt(data,offset, 3));
        offset+=3;
        setLogicalMassMemoryEndAddress(ProtocolUtils.getInt(data,offset, 3));
        offset+=3;
        setActualMassMemoryEndAddress(ProtocolUtils.getInt(data,offset, 3));
        offset+=3;
        setCurrentLogicalMassMemoryAddress(ProtocolUtils.getInt(data,offset, 3));
        offset+=3;
        // reserved 3 bytes
        offset+=3;
        setCurrentMassMemoryRecordNumber(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setCurrentMassMemoryIntervalNumber(ProtocolUtils.getInt(data,offset, 1));
        offset++;
        setNumberOfChannels(ProtocolUtils.getInt(data,offset, 1));
        offset++;
        // reserved 1 byte
        offset++;
        setRecordingIntervalLength(ProtocolUtils.getInt(data,offset, 1));
        offset++;
        setMassMemoryOutageLength(ProtocolUtils.getInt(data,offset, 1));
        offset++;
    }

    public MassMemoryProgramTable[] getMassMemoryProgramTable() {
        return massMemoryProgramTable;
    }

    public void setMassMemoryProgramTable(MassMemoryProgramTable[] massMemoryProgramTable) {
        this.massMemoryProgramTable = massMemoryProgramTable;
    }

    public int getMassMemoryRecordLength() {
        return massMemoryRecordLength;
    }

    public void setMassMemoryRecordLength(int massMemoryRecordLength) {
        this.massMemoryRecordLength = massMemoryRecordLength;
    }

    public int getLogicalMassMemoryStartAddress() {
        return logicalMassMemoryStartAddress;
    }

    public void setLogicalMassMemoryStartAddress(int logicalMassMemoryStartAddress) {
        this.logicalMassMemoryStartAddress = logicalMassMemoryStartAddress;
    }

    public int getLogicalMassMemoryEndAddress() {
        return logicalMassMemoryEndAddress;
    }

    public void setLogicalMassMemoryEndAddress(int logicalMassMemoryEndAddress) {
        this.logicalMassMemoryEndAddress = logicalMassMemoryEndAddress;
    }

    public int getActualMassMemoryEndAddress() {
        return actualMassMemoryEndAddress;
    }

    public void setActualMassMemoryEndAddress(int actualMassMemoryEndAddress) {
        this.actualMassMemoryEndAddress = actualMassMemoryEndAddress;
    }

    public int getCurrentLogicalMassMemoryAddress() {
        return currentLogicalMassMemoryAddress;
    }

    public void setCurrentLogicalMassMemoryAddress(int currentLogicalMassMemoryAddress) {
        this.currentLogicalMassMemoryAddress = currentLogicalMassMemoryAddress;
    }

    public int getCurrentMassMemoryRecordNumber() {
        return currentMassMemoryRecordNumber;
    }

    public void setCurrentMassMemoryRecordNumber(int currentMassMemoryRecordNumber) {
        this.currentMassMemoryRecordNumber = currentMassMemoryRecordNumber;
    }

    public int getCurrentMassMemoryIntervalNumber() {
        return currentMassMemoryIntervalNumber;
    }

    public void setCurrentMassMemoryIntervalNumber(int currentMassMemoryIntervalNumber) {
        this.currentMassMemoryIntervalNumber = currentMassMemoryIntervalNumber;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public void setNumberOfChannels(int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public int getRecordingIntervalLength() {
        return recordingIntervalLength;
    }

    public void setRecordingIntervalLength(int recordingIntervalLength) {
        this.recordingIntervalLength = recordingIntervalLength;
    }

    public int getMassMemoryOutageLength() {
        return massMemoryOutageLength;
    }

    public void setMassMemoryOutageLength(int massMemoryOutageLength) {
        this.massMemoryOutageLength = massMemoryOutageLength;
    }


} // public class RealTimeBasePage extends AbstractBasePage
