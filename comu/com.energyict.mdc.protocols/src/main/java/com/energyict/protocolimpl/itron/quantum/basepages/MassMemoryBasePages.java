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

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MassMemoryBasePages extends AbstractBasePage {

    private final int[] RECORDLENGTHFORCHANNELS=new int[]{107,203,299,395,491,587,779,875,971,1067,1163,1259,1355,1451,1547};

    private int currentMassMemoryRecordOffset; // 3 bytes
    private int currentMassMemoryIntervalNumber; // 1 bytes
    final int MAX_NR_OF_CHANNELS=16;
    private ChannelProgram[] channelPrograms;
    private int numberOfChannels; // 1 byte
    private int nrOfBitsFormat; // 1 byte
    private int totalNROfIntervals; // 1 byte
    private int recordingIntervalLength; // 1 byte in minutes
    private int massMemoryOutageLength; // 1 byte
    private int logicalMassMemoryStartOffset; // 3 bytes
    private int logicalMassMemoryEndOffset; // 3 bytes

    /** Creates a new instance of MassMemoryBasePages */
    public MassMemoryBasePages(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryBasePages:\n");
        for (int i=0;i<getChannelPrograms().length;i++) {
           strBuff.append("       channelPrograms["+i+"]="+getChannelPrograms()[i]+"\n");
        }
        strBuff.append("   currentMassMemoryIntervalNumber=0x"+Integer.toHexString(getCurrentMassMemoryIntervalNumber())+"\n");
        strBuff.append("   currentMassMemoryRecordOffset="+getCurrentMassMemoryRecordOffset()+"\n");
        strBuff.append("   logicalMassMemoryEndOffset=0x"+Integer.toHexString(getLogicalMassMemoryEndOffset())+"\n");
        strBuff.append("   logicalMassMemoryStartOffset=0x"+Integer.toHexString(getLogicalMassMemoryStartOffset())+"\n");
        strBuff.append("   massMemoryOutageLength="+getMassMemoryOutageLength()+"\n");
        strBuff.append("   nrOfBitsFormat="+getNrOfBitsFormat()+"\n");
        strBuff.append("   numberOfChannels="+getNumberOfChannels()+"\n");
        strBuff.append("   recordingIntervalLength="+getRecordingIntervalLength()+"\n");
        strBuff.append("   totalNROfIntervals="+getTotalNROfIntervals()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(22,85-22);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        setCurrentMassMemoryRecordOffset(ProtocolUtils.getInt(data,offset, 3)-getBasePagesFactory().getMemStartAddress());
        offset+=3;
        setCurrentMassMemoryIntervalNumber(ProtocolUtils.getInt(data,offset, 1));
        offset++;
        setChannelPrograms(new ChannelProgram[MAX_NR_OF_CHANNELS]);
        for(int i=0;i<MAX_NR_OF_CHANNELS;i++) {
            getChannelPrograms()[i] = new ChannelProgram(data,offset);
            offset+=ChannelProgram.size();
        }
        setNumberOfChannels(ProtocolUtils.getInt(data,offset, 1));
        offset++;
        setNrOfBitsFormat((int)data[offset]&0xFF);
        offset++;
        setTotalNROfIntervals((int)data[offset]&0xFF);
        offset++;
        setRecordingIntervalLength(ProtocolUtils.getInt(data,offset, 1));
        offset++;
        setMassMemoryOutageLength(ProtocolUtils.getInt(data,offset, 1));
        offset++;

        setLogicalMassMemoryStartOffset(ProtocolUtils.getInt(data,offset, 3)-getBasePagesFactory().getMemStartAddress());
        offset+=3;
        setLogicalMassMemoryEndOffset(ProtocolUtils.getInt(data,offset, 3)-getBasePagesFactory().getMemStartAddress());
        offset+=3;
    }

    public int getLogicalMassMemoryStartOffset() {
        return logicalMassMemoryStartOffset;
    }

    public void setLogicalMassMemoryStartOffset(int logicalMassMemoryStartOffset) {
        this.logicalMassMemoryStartOffset = logicalMassMemoryStartOffset;
    }

    public int getLogicalMassMemoryEndOffset() {
        return logicalMassMemoryEndOffset;
    }

    public void setLogicalMassMemoryEndOffset(int logicalMassMemoryEndOffset) {
        this.logicalMassMemoryEndOffset = logicalMassMemoryEndOffset;
    }

    public int getCurrentMassMemoryRecordOffset() {
        return currentMassMemoryRecordOffset;
    }

    public void setCurrentMassMemoryRecordOffset(int currentMassMemoryRecordOffset) {
        this.currentMassMemoryRecordOffset = currentMassMemoryRecordOffset;
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

    public ChannelProgram[] getChannelPrograms() {
        return channelPrograms;
    }

    public void setChannelPrograms(ChannelProgram[] channelPrograms) {
        this.channelPrograms = channelPrograms;
    }

    public int getNrOfBitsFormat() {
        return nrOfBitsFormat;
    }

    public void setNrOfBitsFormat(int nrOfBitsFormat) {
        this.nrOfBitsFormat = nrOfBitsFormat;
    }

    public int getTotalNROfIntervals() {
        return totalNROfIntervals;
    }

    public void setTotalNROfIntervals(int totalNROfIntervals) {
        this.totalNROfIntervals = totalNROfIntervals;
    }

    public int getMassMemoryRecordLength() {
        if (getNumberOfChannels() > 0)
            return RECORDLENGTHFORCHANNELS[getNumberOfChannels()-1];
        else
            return 0;
    }

} // public class RealTimeBasePage extends AbstractBasePage
