/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterStatus.java
 *
 * Created on 7 juli 2004, 11:31
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class MeterStatus extends AbstractLogicalAddress {

    long batteryLifeCounter; // in seconds
    int status; // bit 0: RED status, 0=clear, 1=set
                // bit 1: Default status, 0=not in, 1=in
                // bit 2: factory mode, 0=not in, 1=in
                // bit 3: calibration led units, 0=kWh, 1=kvarh
                // bit 4: MC3PDN in default, 0=not in, 1=in
    int batteryVoltage; // ADC count
    int currentSecurityLevel; // 0..5=A..F
    int detectedMemorySize; // A=16K, B=24K, C=32K, D=40K, E=48K, F=56K, G=64K

    /** Creates a new instance of MeterStatus */
    public MeterStatus(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }

    public String toString() {
       return "MeterStatus: battlife="+getBatteryLifeCounter()+", status="+Integer.toHexString(getStatus())+", battVolt="+getBatteryVoltage()+", currSecurityLvl="+getCurrentSecurityLevel()+", detMemSize="+getDetectedMemorySize();
    }


    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
        setBatteryLifeCounter(ProtocolUtils.getLongLE(data,0,4));
        setStatus((int)data[4]);
        setBatteryVoltage((int)data[5]&0xFF);
        setCurrentSecurityLevel((int)data[6]);
        setDetectedMemorySize((int)data[7]);
    }


    /**
     * Getter for property status.
     * @return Value of property status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Setter for property status.
     * @param status New value of property status.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Getter for property batteryVoltage.
     * @return Value of property batteryVoltage.
     */
    public int getBatteryVoltage() {
        return batteryVoltage;
    }

    /**
     * Setter for property batteryVoltage.
     * @param batteryVoltage New value of property batteryVoltage.
     */
    public void setBatteryVoltage(int batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    /**
     * Getter for property currentSecurityLevel.
     * @return Value of property currentSecurityLevel.
     */
    public int getCurrentSecurityLevel() {
        return currentSecurityLevel;
    }

    /**
     * Setter for property currentSecurityLevel.
     * @param currentSecurityLevel New value of property currentSecurityLevel.
     */
    public void setCurrentSecurityLevel(int currentSecurityLevel) {
        this.currentSecurityLevel = currentSecurityLevel;
    }



    /**
     * Getter for property batteryLifeCounter.
     * @return Value of property batteryLifeCounter.
     */
    public long getBatteryLifeCounter() {
        return batteryLifeCounter;
    }

    /**
     * Setter for property batteryLifeCounter.
     * @param batteryLifeCounter New value of property batteryLifeCounter.
     */
    public void setBatteryLifeCounter(long batteryLifeCounter) {
        this.batteryLifeCounter = batteryLifeCounter;
    }

    /**
     * Getter for property detectedMemorySize.
     * @return Value of property detectedMemorySize.
     */
    public int getDetectedMemorySize() {
        return detectedMemorySize;
    }

    /**
     * Setter for property detectedMemorySize.
     * @param detectedMemorySize New value of property detectedMemorySize.
     */
    public void setDetectedMemorySize(int detectedMemorySize) {
        this.detectedMemorySize = detectedMemorySize;
    }

}
