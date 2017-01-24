/*
 * ConstantsDataRead.java
 *
 * Created on 2 november 2006, 16:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class ClockRelatedDataRead extends AbstractDataRead {


    private int daysSinceLastDemandReset; // UINT16
    private int daysSinceLastTimeTestModeWasEntered; // UINT16
    private long timeOfLastPowerOutage; // (in seconds since 00:00:00 01/01/2000) UINT32
    private long timeOfLastInterrogation; // (in seconds since 00:00:00 01/01/2000) UINT32
    private long daysOnBattery; // UINT32
    private int currentBatteryReading; // UINT16
    private int goodBatteryReading; // UINT16
    private int daylightSavingsTimeIsConfigured; // (True/False); // UINT8


    /** Creates a new instance of ConstantsDataRead */
    public ClockRelatedDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ClockRelatedDataRead:\n");
        strBuff.append("   currentBatteryReading="+getCurrentBatteryReading()+"\n");
        strBuff.append("   daylightSavingsTimeIsConfigured="+getDaylightSavingsTimeIsConfigured()+"\n");
        strBuff.append("   daysOnBattery="+getDaysOnBattery()+"\n");
        strBuff.append("   daysSinceLastDemandReset="+getDaysSinceLastDemandReset()+"\n");
        strBuff.append("   daysSinceLastTimeTestModeWasEntered="+getDaysSinceLastTimeTestModeWasEntered()+"\n");
        strBuff.append("   goodBatteryReading="+getGoodBatteryReading()+"\n");
        strBuff.append("   timeOfLastInterrogation="+getTimeOfLastInterrogation()+"\n");
        strBuff.append("   timeOfLastPowerOutage="+getTimeOfLastPowerOutage()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setDaysSinceLastDemandReset(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setDaysSinceLastTimeTestModeWasEntered(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setTimeOfLastPowerOutage(C12ParseUtils.getInt(data,offset,4, dataOrder));
        offset+=4;
        setTimeOfLastInterrogation(C12ParseUtils.getInt(data,offset,4, dataOrder));
        offset+=4;
        setDaysOnBattery(C12ParseUtils.getInt(data,offset,4, dataOrder));
        offset+=4;
        setCurrentBatteryReading(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setGoodBatteryReading(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setDaylightSavingsTimeIsConfigured(C12ParseUtils.getInt(data,offset++));
    }

    protected void prepareBuild() throws IOException {

        long[] lids = new long[]{LogicalIDFactory.findLogicalId("DAYS_SINCE_DEMAND_RESET").getId(),
                                 LogicalIDFactory.findLogicalId("DAYS_SINCE_LAST_TEST").getId(),
                                 LogicalIDFactory.findLogicalId("TIME_OF_LAST_OUTAGE").getId(),
                                 LogicalIDFactory.findLogicalId("TIME_OF_LAST_INTERROGATION").getId(),
                                 LogicalIDFactory.findLogicalId("DAYS_ON_BATTERY").getId(),
                                 LogicalIDFactory.findLogicalId("CURRENT_BATTERY_READING").getId(),
                                 LogicalIDFactory.findLogicalId("GOOD_BATTERY_READING").getId(),
                                 LogicalIDFactory.findLogicalId("DST_CONFIGURED").getId()};



        setDataReadDescriptor(new DataReadDescriptor(0x00, 0x08, lids));

    } // protected void prepareBuild() throws IOException

    public int getDaysSinceLastDemandReset() {
        return daysSinceLastDemandReset;
    }

    public void setDaysSinceLastDemandReset(int daysSinceLastDemandReset) {
        this.daysSinceLastDemandReset = daysSinceLastDemandReset;
    }

    public int getDaysSinceLastTimeTestModeWasEntered() {
        return daysSinceLastTimeTestModeWasEntered;
    }

    public void setDaysSinceLastTimeTestModeWasEntered(int daysSinceLastTimeTestModeWasEntered) {
        this.daysSinceLastTimeTestModeWasEntered = daysSinceLastTimeTestModeWasEntered;
    }

    public long getTimeOfLastPowerOutage() {
        return timeOfLastPowerOutage;
    }

    public void setTimeOfLastPowerOutage(long timeOfLastPowerOutage) {
        this.timeOfLastPowerOutage = timeOfLastPowerOutage;
    }

    public long getTimeOfLastInterrogation() {
        return timeOfLastInterrogation;
    }

    public void setTimeOfLastInterrogation(long timeOfLastInterrogation) {
        this.timeOfLastInterrogation = timeOfLastInterrogation;
    }

    public long getDaysOnBattery() {
        return daysOnBattery;
    }

    public void setDaysOnBattery(long daysOnBattery) {
        this.daysOnBattery = daysOnBattery;
    }

    public int getCurrentBatteryReading() {
        return currentBatteryReading;
    }

    public void setCurrentBatteryReading(int currentBatteryReading) {
        this.currentBatteryReading = currentBatteryReading;
    }

    public int getGoodBatteryReading() {
        return goodBatteryReading;
    }

    public void setGoodBatteryReading(int goodBatteryReading) {
        this.goodBatteryReading = goodBatteryReading;
    }

    public int getDaylightSavingsTimeIsConfigured() {
        return daylightSavingsTimeIsConfigured;
    }

    public void setDaylightSavingsTimeIsConfigured(int daylightSavingsTimeIsConfigured) {
        this.daylightSavingsTimeIsConfigured = daylightSavingsTimeIsConfigured;
    }

} // public class ConstantsDataRead extends AbstractDataRead
