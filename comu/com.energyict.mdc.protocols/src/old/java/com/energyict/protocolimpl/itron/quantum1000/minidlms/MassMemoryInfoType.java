/*
 * MassMemoryInfoType.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class MassMemoryInfoType {

    private boolean runFlag; // BOOLEAN
    private long fileSize; // UNSIGNE32,
    private long currentIntervalNumber; // UNSIGNED32,
    private int numberOfSecsRemaining; // UNSIGNED16,
    private boolean shortInterval; // BOOLEAN,
    private boolean longInterval; // BOOLEAN,
    private boolean outageInterval; // BOOLEAN,
    private boolean testMode; // BOOLEAN,
    private boolean externalEOI; // BOOLEAN,
    private boolean clockError; // BOOLEAN,
    private Date nextEOITime; // DATE_AND_TIME,
    private Result lastErrorResult; // RESULT,
    private Result powerUpResult; // RESULT,
    private long errorFlag; // UNSIGNED32,
    private long[] channelPulses = new long[24]; //96
    private boolean isStatusOK; // BOOLEAN,
    private boolean isConfigOK; // BOOLEAN,
    private boolean isFileStatusOK; // BOOLEAN,
    private boolean timeAdjustInterval; // BOOLEAN,

    /**
     * Creates a new instance of MassMemoryInfoType
     */
    public MassMemoryInfoType(byte[] data,int offset,TimeZone timeZone) throws IOException {
        setRunFlag(ProtocolUtils.getInt(data,offset++,1) == 1);
        setFileSize(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setCurrentIntervalNumber(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setNumberOfSecsRemaining(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setShortInterval(ProtocolUtils.getInt(data,offset++,1) == 1);
        setLongInterval(ProtocolUtils.getInt(data,offset++,1) == 1);
        setOutageInterval(ProtocolUtils.getInt(data,offset++,1) == 1);
        setTestMode(ProtocolUtils.getInt(data,offset++,1) == 1);
        setExternalEOI(ProtocolUtils.getInt(data,offset++,1) == 1);
        setClockError(ProtocolUtils.getInt(data,offset++,1) == 1);
        setNextEOITime(Utils.getDateFromDateTime(data,offset, timeZone));
        offset+=Utils.getDateTimeSize();
        setLastErrorResult(new Result(data, offset));
        offset+=Result.size();
        setPowerUpResult(new Result(data, offset));
        offset+=Result.size();
        setErrorFlag(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        for (int i=0;i<getChannelPulses().length;i++) {
            getChannelPulses()[i] = ProtocolUtils.getInt(data,offset,4);
            offset+=4;
        }
        setIsStatusOK(ProtocolUtils.getInt(data,offset++,1) == 1);
        setIsConfigOK(ProtocolUtils.getInt(data,offset++,1) == 1);
        setIsFileStatusOK(ProtocolUtils.getInt(data,offset++,1) == 1);
        setTimeAdjustInterval(ProtocolUtils.getInt(data,offset++,1) == 1);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryInfoType:\n");
        for (int i=0;i<getChannelPulses().length;i++) {
            strBuff.append("       channelPulses["+i+"]="+getChannelPulses()[i]+"\n");
        }
        strBuff.append("   clockError="+isClockError()+"\n");
        strBuff.append("   currentIntervalNumber="+getCurrentIntervalNumber()+"\n");
        strBuff.append("   errorFlag="+getErrorFlag()+"\n");
        strBuff.append("   externalEOI="+isExternalEOI()+"\n");
        strBuff.append("   fileSize="+getFileSize()+"\n");
        strBuff.append("   isConfigOK="+isIsConfigOK()+"\n");
        strBuff.append("   isFileStatusOK="+isIsFileStatusOK()+"\n");
        strBuff.append("   isStatusOK="+isIsStatusOK()+"\n");
        strBuff.append("   lastErrorResult="+getLastErrorResult()+"\n");
        strBuff.append("   longInterval="+isLongInterval()+"\n");
        strBuff.append("   nextEOITime="+getNextEOITime()+"\n");
        strBuff.append("   numberOfSecsRemaining="+getNumberOfSecsRemaining()+"\n");
        strBuff.append("   outageInterval="+isOutageInterval()+"\n");
        strBuff.append("   powerUpResult="+getPowerUpResult()+"\n");
        strBuff.append("   runFlag="+isRunFlag()+"\n");
        strBuff.append("   shortInterval="+isShortInterval()+"\n");
        strBuff.append("   testMode="+isTestMode()+"\n");
        strBuff.append("   timeAdjustInterval="+isTimeAdjustInterval()+"\n");
        return strBuff.toString();
    }

    static public int size() {
      return 35+96+4;
    }

    public boolean isRunFlag() {
        return runFlag;
    }

    public void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getCurrentIntervalNumber() {
        return currentIntervalNumber;
    }

    public void setCurrentIntervalNumber(long currentIntervalNumber) {
        this.currentIntervalNumber = currentIntervalNumber;
    }

    public int getNumberOfSecsRemaining() {
        return numberOfSecsRemaining;
    }

    public void setNumberOfSecsRemaining(int numberOfSecsRemaining) {
        this.numberOfSecsRemaining = numberOfSecsRemaining;
    }

    public boolean isShortInterval() {
        return shortInterval;
    }

    public void setShortInterval(boolean shortInterval) {
        this.shortInterval = shortInterval;
    }

    public boolean isLongInterval() {
        return longInterval;
    }

    public void setLongInterval(boolean longInterval) {
        this.longInterval = longInterval;
    }

    public boolean isOutageInterval() {
        return outageInterval;
    }

    public void setOutageInterval(boolean outageInterval) {
        this.outageInterval = outageInterval;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public boolean isExternalEOI() {
        return externalEOI;
    }

    public void setExternalEOI(boolean externalEOI) {
        this.externalEOI = externalEOI;
    }

    public boolean isClockError() {
        return clockError;
    }

    public void setClockError(boolean clockError) {
        this.clockError = clockError;
    }

    public Date getNextEOITime() {
        return nextEOITime;
    }

    public void setNextEOITime(Date nextEOITime) {
        this.nextEOITime = nextEOITime;
    }

    public Result getLastErrorResult() {
        return lastErrorResult;
    }

    public void setLastErrorResult(Result lastErrorResult) {
        this.lastErrorResult = lastErrorResult;
    }

    public Result getPowerUpResult() {
        return powerUpResult;
    }

    public void setPowerUpResult(Result powerUpResult) {
        this.powerUpResult = powerUpResult;
    }

    public long getErrorFlag() {
        return errorFlag;
    }

    public void setErrorFlag(long errorFlag) {
        this.errorFlag = errorFlag;
    }

    public long[] getChannelPulses() {
        return channelPulses;
    }

    public void setChannelPulses(long[] channelPulses) {
        this.channelPulses = channelPulses;
    }

    public boolean isIsStatusOK() {
        return isStatusOK;
    }

    public void setIsStatusOK(boolean isStatusOK) {
        this.isStatusOK = isStatusOK;
    }

    public boolean isIsConfigOK() {
        return isConfigOK;
    }

    public void setIsConfigOK(boolean isConfigOK) {
        this.isConfigOK = isConfigOK;
    }

    public boolean isIsFileStatusOK() {
        return isFileStatusOK;
    }

    public void setIsFileStatusOK(boolean isFileStatusOK) {
        this.isFileStatusOK = isFileStatusOK;
    }

    public boolean isTimeAdjustInterval() {
        return timeAdjustInterval;
    }

    public void setTimeAdjustInterval(boolean timeAdjustInterval) {
        this.timeAdjustInterval = timeAdjustInterval;
    }


}
