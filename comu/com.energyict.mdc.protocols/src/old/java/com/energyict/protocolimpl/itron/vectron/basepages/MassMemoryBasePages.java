/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;
/**
 *
 * @author Koen
 */
public class MassMemoryBasePages extends AbstractBasePage {

    private BigDecimal[] channelPulseWidths = new BigDecimal[2]; // max 2 channels
    private int profileInterval; // in minutes
    private int logicalStartAddress;
    private int logicalEndAddress;
    private int outageLength; // in seconds
    private int currentRecordNumber;
    private int currentIntervalNumber;
    private int leftoverOutageTime;
    private int currentRecordAddress;
    private Calendar coldStartTime;
    private int nrOfChannels;
    private int[] channelRegisterAddresses = new int[2]; // max 2 channels
    private int intervalTimer;

    /** Creates a new instance of RealTimeBasePage */
    public MassMemoryBasePages(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryBasePages:\n");
        for (int i=0;i<getChannelPulseWidths().length;i++) {
            strBuff.append("       channelPulseWidths["+i+"]="+getChannelPulseWidths()[i]+"\n");
        }
        for (int i=0;i<getChannelRegisterAddresses().length;i++) {
            strBuff.append("       channelRegisterAddresses["+i+"]=0x"+Integer.toHexString(getChannelRegisterAddresses()[i])+"\n");
        }
        strBuff.append("   coldStartTime="+getColdStartTime().getTime()+"\n");
        strBuff.append("   currentIntervalNumber="+getCurrentIntervalNumber()+"\n");
        strBuff.append("   currentRecordAddress=0x"+Integer.toHexString(getCurrentRecordAddress())+"\n");
        strBuff.append("   currentRecordNumber="+getCurrentRecordNumber()+"\n");
        strBuff.append("   intervalTimer="+getIntervalTimer()+"\n");
        strBuff.append("   leftoverOutageTime="+getLeftoverOutageTime()+"\n");
        strBuff.append("   logicalEndAddress=0x"+Integer.toHexString(getLogicalEndAddress())+"\n");
        strBuff.append("   logicalStartAddress=0x"+Integer.toHexString(getLogicalStartAddress())+"\n");
        strBuff.append("   nrOfChannels="+getNrOfChannels()+"\n");
        strBuff.append("   outageLength="+getOutageLength()+"\n");
        strBuff.append("   profileInterval="+getProfileInterval()+"\n");
        return strBuff.toString();
    }

    public int getMassMemoryRecordLength() {
        if (getNrOfChannels() == 1) {
            return 108;
        }
        else if (getNrOfChannels() == 2) {
            return 204;
        }

        return 0;

    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x2500,0x2541-0x2500);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        getChannelPulseWidths()[0] = ParseUtils.convertBCDFixedPoint(data,offset,4,8);
        offset+=4;
        setProfileInterval((int)ParseUtils.getBCD2Long(data,offset, 1));
        offset++;
        setLogicalStartAddress(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setLogicalEndAddress(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setOutageLength(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        offset++; // skip unused byte address 250b
        offset+=14; // skip load research id address 250c
        setCurrentRecordNumber(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setCurrentIntervalNumber(ProtocolUtils.getInt(data,offset,1));
        offset++;
        offset+=2; // skip reserved address 251d
        setLeftoverOutageTime((int)ParseUtils.getBCD2Long(data,offset, 3));
        offset+=3;
        setCurrentRecordAddress(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        offset+=5; // skip reserved address 2524
        offset+=3; // skip reserved address 2529
        buildDate(data,offset);
        offset+=7;
        offset+=2; // skip reserved address 2533
        setNrOfChannels(ProtocolUtils.getInt(data,offset, 1));
        offset++;
        getChannelRegisterAddresses()[0] = ProtocolUtils.getInt(data,offset, 1);
        offset++;
        getChannelRegisterAddresses()[1] = ProtocolUtils.getInt(data,offset, 1);
        offset++;
        getChannelPulseWidths()[1] = ParseUtils.convertBCDFixedPoint(data,offset,4,8);
        offset+=4;
        int temp = ProtocolUtils.getInt(data,offset, 1);
        offset++;
        setIntervalTimer(((temp<<4)&0xF0) + ((temp>>4)&0x0F));
        offset+=2; // skip 2 bytes address 253D
        offset+=2; // skip 2 bytes address 253F
    }


    private void buildDate(byte[] data,int offset) throws IOException {
        TimeZone tz = getBasePagesFactory().getProtocolLink().getTimeZone();

        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);

        setColdStartTime(ProtocolUtils.getCleanCalendar(tz));
        getColdStartTime().set(Calendar.DAY_OF_WEEK,(int)ParseUtils.getBCD2Long(data,6,1));
        getColdStartTime().set(Calendar.SECOND,(int)ParseUtils.getBCD2Long(data,5, 1));
        getColdStartTime().set(Calendar.MINUTE,(int)ParseUtils.getBCD2Long(data,4, 1));
        getColdStartTime().set(Calendar.HOUR_OF_DAY,(int)ParseUtils.getBCD2Long(data,3, 1));
        getColdStartTime().set(Calendar.DAY_OF_MONTH,(int)ParseUtils.getBCD2Long(data,2, 1));
        getColdStartTime().set(Calendar.MONTH,(int)ParseUtils.getBCD2Long(data,1, 1)-1);
        int year = (int)ParseUtils.getBCD2Long(data,0, 1);
        getColdStartTime().set(Calendar.YEAR,year>50?year+1900:year+2000);

    }



    public int getProfileInterval() {
        return profileInterval;
    }

    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    public int getLogicalStartAddress() {
        return logicalStartAddress;
    }

    public void setLogicalStartAddress(int logicalStartAddress) {
        this.logicalStartAddress = logicalStartAddress;
    }

    public int getLogicalEndAddress() {
        return logicalEndAddress;
    }

    public void setLogicalEndAddress(int logicalEndAddress) {
        this.logicalEndAddress = logicalEndAddress;
    }

    public int getOutageLength() {
        return outageLength;
    }

    public void setOutageLength(int outageLength) {
        this.outageLength = outageLength;
    }

    public int getCurrentRecordNumber() {
        return currentRecordNumber;
    }

    public void setCurrentRecordNumber(int currentRecordNumber) {
        this.currentRecordNumber = currentRecordNumber;
    }

    public int getCurrentIntervalNumber() {
        return currentIntervalNumber;
    }

    public void setCurrentIntervalNumber(int currentIntervalNumber) {
        this.currentIntervalNumber = currentIntervalNumber;
    }

    public int getLeftoverOutageTime() {
        return leftoverOutageTime;
    }

    public void setLeftoverOutageTime(int leftoverOutageTime) {
        this.leftoverOutageTime = leftoverOutageTime;
    }

    public int getCurrentRecordAddress() {
        return currentRecordAddress;
    }

    public void setCurrentRecordAddress(int currentRecordAddress) {
        this.currentRecordAddress = currentRecordAddress;
    }

    public Calendar getColdStartTime() {
        return coldStartTime;
    }

    public void setColdStartTime(Calendar coldStartTime) {
        this.coldStartTime = coldStartTime;
    }

    public BigDecimal[] getChannelPulseWidths() {
        return channelPulseWidths;
    }

    public void setChannelPulseWidths(BigDecimal[] channelPulseWidths) {
        this.channelPulseWidths = channelPulseWidths;
    }

    public int getNrOfChannels() {
        return nrOfChannels;
    }

    public void setNrOfChannels(int nrOfChannels) {
        this.nrOfChannels = nrOfChannels;
    }

    public int[] getChannelRegisterAddresses() {
        return channelRegisterAddresses;
    }

    public void setChannelRegisterAddresses(int[] channelRegisterAddresses) {
        this.channelRegisterAddresses = channelRegisterAddresses;
    }

    public int getIntervalTimer() {
        return intervalTimer;
    }

    public void setIntervalTimer(int intervalTimer) {
        this.intervalTimer = intervalTimer;
    }

} // public class RealTimeBasePage extends AbstractBasePage
