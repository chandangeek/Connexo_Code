/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EndDeviceStdStatus1Bitfield.java
 *
 * Created on 23 februari 2006, 15:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class EndDeviceStdStatus1Bitfield {

    private boolean unprogrammedFlag;
    private boolean configurationErrorFlag;
    private boolean selfCheckErrorFlag;
    private boolean ramFailureFlag;
    private boolean romFailureFlag;
    private boolean nonvolMemFailureFlag;
    private boolean clockErrorFlag;
    private boolean measurementErrorFlag;
    private boolean lowBatteryFlag;
    private boolean lowLossPotentialFlag;
    private boolean demandOverflowFlag;
    private boolean powerFailureFlag;

    /** Creates a new instance of EndDeviceStdStatus1Bitfield */
    public EndDeviceStdStatus1Bitfield(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        int temp = C12ParseUtils.getInt(data,0);
        setUnprogrammedFlag((temp & 0x01) == 0x01);
        setConfigurationErrorFlag((temp & 0x02) == 0x02);
        setSelfCheckErrorFlag((temp & 0x04) == 0x04);
        setRamFailureFlag((temp & 0x08) == 0x08);
        setRomFailureFlag((temp & 0x10) == 0x10);
        setNonvolMemFailureFlag((temp & 0x20) == 020);
        setClockErrorFlag((temp & 0x40) == 0x40);
        setMeasurementErrorFlag((temp & 0x80) == 0x80);
        setLowBatteryFlag((temp & 0x100) == 0x100);
        setLowLossPotentialFlag((temp & 0x200) == 0x200);
        setDemandOverflowFlag((temp & 0x400) == 0x400);
        setPowerFailureFlag((temp & 0x800) == 0x800);


    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EndDeviceStdStatus1Bitfield:\n");
        strBuff.append("   clockErrorFlag="+isClockErrorFlag()+"\n");
        strBuff.append("   configurationErrorFlag="+isConfigurationErrorFlag()+"\n");
        strBuff.append("   demandOverflowFlag="+isDemandOverflowFlag()+"\n");
        strBuff.append("   lowBatteryFlag="+isLowBatteryFlag()+"\n");
        strBuff.append("   lowLossPotentialFlag="+isLowLossPotentialFlag()+"\n");
        strBuff.append("   measurementErrorFlag="+isMeasurementErrorFlag()+"\n");
        strBuff.append("   nonvolMemFailureFlag="+isNonvolMemFailureFlag()+"\n");
        strBuff.append("   powerFailureFlag="+isPowerFailureFlag()+"\n");
        strBuff.append("   ramFailureFlag="+isRamFailureFlag()+"\n");
        strBuff.append("   romFailureFlag="+isRomFailureFlag()+"\n");
        strBuff.append("   selfCheckErrorFlag="+isSelfCheckErrorFlag()+"\n");
        strBuff.append("   unprogrammedFlag="+isUnprogrammedFlag()+"\n");
        return strBuff.toString();
    }


    static public int getSize(TableFactory tableFactory) throws IOException {
        return 2;
    }

    public boolean isUnprogrammedFlag() {
        return unprogrammedFlag;
    }

    public void setUnprogrammedFlag(boolean unprogrammedFlag) {
        this.unprogrammedFlag = unprogrammedFlag;
    }

    public boolean isConfigurationErrorFlag() {
        return configurationErrorFlag;
    }

    public void setConfigurationErrorFlag(boolean configurationErrorFlag) {
        this.configurationErrorFlag = configurationErrorFlag;
    }

    public boolean isSelfCheckErrorFlag() {
        return selfCheckErrorFlag;
    }

    public void setSelfCheckErrorFlag(boolean selfCheckErrorFlag) {
        this.selfCheckErrorFlag = selfCheckErrorFlag;
    }

    public boolean isRamFailureFlag() {
        return ramFailureFlag;
    }

    public void setRamFailureFlag(boolean ramFailureFlag) {
        this.ramFailureFlag = ramFailureFlag;
    }

    public boolean isRomFailureFlag() {
        return romFailureFlag;
    }

    public void setRomFailureFlag(boolean romFailureFlag) {
        this.romFailureFlag = romFailureFlag;
    }

    public boolean isNonvolMemFailureFlag() {
        return nonvolMemFailureFlag;
    }

    public void setNonvolMemFailureFlag(boolean nonvolMemFailureFlag) {
        this.nonvolMemFailureFlag = nonvolMemFailureFlag;
    }

    public boolean isClockErrorFlag() {
        return clockErrorFlag;
    }

    public void setClockErrorFlag(boolean clockErrorFlag) {
        this.clockErrorFlag = clockErrorFlag;
    }

    public boolean isMeasurementErrorFlag() {
        return measurementErrorFlag;
    }

    public void setMeasurementErrorFlag(boolean measurementErrorFlag) {
        this.measurementErrorFlag = measurementErrorFlag;
    }

    public boolean isLowBatteryFlag() {
        return lowBatteryFlag;
    }

    public void setLowBatteryFlag(boolean lowBatteryFlag) {
        this.lowBatteryFlag = lowBatteryFlag;
    }

    public boolean isLowLossPotentialFlag() {
        return lowLossPotentialFlag;
    }

    public void setLowLossPotentialFlag(boolean lowLossPotentialFlag) {
        this.lowLossPotentialFlag = lowLossPotentialFlag;
    }

    public boolean isDemandOverflowFlag() {
        return demandOverflowFlag;
    }

    public void setDemandOverflowFlag(boolean demandOverflowFlag) {
        this.demandOverflowFlag = demandOverflowFlag;
    }

    public boolean isPowerFailureFlag() {
        return powerFailureFlag;
    }

    public void setPowerFailureFlag(boolean powerFailureFlag) {
        this.powerFailureFlag = powerFailureFlag;
    }
}
