/*
 * SelfReadGeneralInformation.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class SelfReadGeneralInformation extends AbstractDataDefinition {

    private Date nextScheduledSelfRead; // DATE_AND_TIME,
    private int actionForNextScheduledSR; // 0 = No Action, 1 = Demand Reset, 2 = Digital State Output, 3 = Demand Reset & Digital State Output
    private Date timeOfLastSelfRead; // DATE_AND_TIME,

    /**
     * 0 = None
     * 1 = Scheduled Date And Time
     * 2 = Scheduled Minute
     * 4 = Scheduled Monthly
     * 8 = Scheduled Weekly
     * 16 = Digital State Input Active
     * 32 = On Line Serial Command
     * 64 = Demand Reset
     * 128 = Season Change
     * 256 = Entered Test Mode
     * 512 = Exited Test Mode
     * 1024 = TOU Current to Latent Switch
     * 32768 = Delayed by EPF
     * ALL OTHERS = Multiple Scheduled Triggers
     */
    private int reasonOfLastSelfRead; // UNSIGNED16,

    /**
     * 0 = No Action
     * 1 = Demand Reset
     * 2 = Digital State Output
     * 3 = Demand Reset & Digital State Output
     * ALL OTHERS = Reserved
     */
    private int  actionCodeOfLastSelfRead; // UNSIGNED8,
    private int numberOfSelfRead; // UNSIGNED16,
    private long blockSize; // UNSIGNED32,
    private long fileSize; // UNSIGNED32,
    private int numRecordsPerFile; // UNSIGNED16

    /** Creates a new instance of SelfReadGeneralInformation */
    public SelfReadGeneralInformation(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadGeneralInformation:\n");
        strBuff.append("   actionCodeOfLastSelfRead="+getActionCodeOfLastSelfRead()+"\n");
        strBuff.append("   actionForNextScheduledSR="+getActionForNextScheduledSR()+"\n");
        strBuff.append("   blockSize="+getBlockSize()+"\n");
        strBuff.append("   fileSize="+getFileSize()+"\n");
        strBuff.append("   nextScheduledSelfRead="+getNextScheduledSelfRead()+"\n");
        strBuff.append("   numRecordsPerFile="+getNumRecordsPerFile()+"\n");
        strBuff.append("   numberOfSelfRead="+getNumberOfSelfRead()+"\n");
        strBuff.append("   reasonOfLastSelfRead="+getReasonOfLastSelfRead()+"\n");
        strBuff.append("   timeOfLastSelfRead="+getTimeOfLastSelfRead()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 0x0048; // 72 DLMS_SELF_READ_GENERAL_INFORMATION
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setNextScheduledSelfRead(Utils.getDateFromDateTime(data,offset,getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone())); // DATE_AND_TIME,
        offset+=Utils.getDateTimeSize();
        setActionForNextScheduledSR(ProtocolUtils.getInt(data,offset++,1));
        setTimeOfLastSelfRead(Utils.getDateFromDateTime(data,offset,getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone())); // DATE_AND_TIME,
        offset+=Utils.getDateTimeSize();
        setReasonOfLastSelfRead(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setActionCodeOfLastSelfRead(ProtocolUtils.getInt(data,offset++,1));
        setNumberOfSelfRead(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setBlockSize(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
        setFileSize(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
        setNumRecordsPerFile(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
    }

    public int numberOfSelfReadSets() {
        return getNumberOfSelfRead()>getNumRecordsPerFile()?getNumRecordsPerFile():getNumberOfSelfRead();
    }

    public Date getNextScheduledSelfRead() {
        return nextScheduledSelfRead;
    }

    public void setNextScheduledSelfRead(Date nextScheduledSelfRead) {
        this.nextScheduledSelfRead = nextScheduledSelfRead;
    }

    public int getActionForNextScheduledSR() {
        return actionForNextScheduledSR;
    }

    public void setActionForNextScheduledSR(int actionForNextScheduledSR) {
        this.actionForNextScheduledSR = actionForNextScheduledSR;
    }

    public Date getTimeOfLastSelfRead() {
        return timeOfLastSelfRead;
    }

    public void setTimeOfLastSelfRead(Date timeOfLastSelfRead) {
        this.timeOfLastSelfRead = timeOfLastSelfRead;
    }

    public int getReasonOfLastSelfRead() {
        return reasonOfLastSelfRead;
    }

    public void setReasonOfLastSelfRead(int reasonOfLastSelfRead) {
        this.reasonOfLastSelfRead = reasonOfLastSelfRead;
    }

    public int getActionCodeOfLastSelfRead() {
        return actionCodeOfLastSelfRead;
    }

    public void setActionCodeOfLastSelfRead(int actionCodeOfLastSelfRead) {
        this.actionCodeOfLastSelfRead = actionCodeOfLastSelfRead;
    }

    public int getNumberOfSelfRead() {
        return numberOfSelfRead;
    }

    public void setNumberOfSelfRead(int numberOfSelfRead) {
        this.numberOfSelfRead = numberOfSelfRead;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getNumRecordsPerFile() {
        return numRecordsPerFile;
    }

    public void setNumRecordsPerFile(int numRecordsPerFile) {
        this.numRecordsPerFile = numRecordsPerFile;
    }
}
