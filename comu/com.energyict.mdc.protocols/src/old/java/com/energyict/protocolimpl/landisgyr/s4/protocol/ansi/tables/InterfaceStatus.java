/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TableTemplate.java
 *
 * Created July 2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.ActualSourcesLimitingTable;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class InterfaceStatus extends AbstractTable {



    private int powerOutageNumber; // POWER_OUTAGE_NUMBER : UINT8;
    private int numberOfTimesProgrammed; // NUMBER_TIMES_PROGRAMMED : UINT8;
    private int manualTestMode; // MANUAL_TEST_MODE_ACT : UINT8;
    private int opticalTestMode; // OPTICAL_TEST_MODE_ACT : UINT8;
    private Date lastProgrammingTime; // LAST_PROGRAMMING_TIME : STIME_DATE;
    private long batteryCarryoverTime; // BATTERY_CARRYOVER_TIME : ARRAY[4] OF UINT8;

    /** Creates a new instance of TableTemplate */
    public InterfaceStatus(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(18,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("InterfaceStatus:\n");
        strBuff.append("   batteryCarryoverTime="+getBatteryCarryoverTime()+"\n");
        strBuff.append("   lastProgrammingTime="+getLastProgrammingTime()+"\n");
        strBuff.append("   manualTestMode="+getManualTestMode()+"\n");
        strBuff.append("   numberOfTimesProgrammed="+getNumberOfTimesProgrammed()+"\n");
        strBuff.append("   opticalTestMode="+getOpticalTestMode()+"\n");
        strBuff.append("   powerOutageNumber="+getPowerOutageNumber()+"\n");
        return strBuff.toString();
    }


    protected void parse(byte[] tableData) throws IOException {
        ConfigurationTable cfgt = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualSourcesLimitingTable aslt = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable();
        int offset=0;
        setPowerOutageNumber(C12ParseUtils.getInt(tableData,offset++,1, cfgt.getDataOrder())); // POWER_OUTAGE_NUMBER : UINT8;
        setNumberOfTimesProgrammed(C12ParseUtils.getInt(tableData,offset++,1, cfgt.getDataOrder())); // NUMBER_TIMES_PROGRAMMED : UINT8;
        setManualTestMode(C12ParseUtils.getInt(tableData,offset++,1, cfgt.getDataOrder())); // MANUAL_TEST_MODE_ACT : UINT8;
        setOpticalTestMode(C12ParseUtils.getInt(tableData,offset++,1, cfgt.getDataOrder())); // OPTICAL_TEST_MODE_ACT : UINT8;
        setLastProgrammingTime(C12ParseUtils.getDateFromSTime(tableData, offset, cfgt.getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(), cfgt.getDataOrder())); // LAST_PROGRAMMING_TIME : STIME_DATE;
        offset += C12ParseUtils.getSTimeSize(cfgt.getTimeFormat());

        setBatteryCarryoverTime(C12ParseUtils.getLong(tableData, offset, 4, cfgt.getDataOrder())); // BATTERY_CARRYOVER_TIME : ARRAY[4] OF UINT8;
        offset+=4;

    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    public int getPowerOutageNumber() {
        return powerOutageNumber;
    }

    public void setPowerOutageNumber(int powerOutageNumber) {
        this.powerOutageNumber = powerOutageNumber;
    }

    public int getNumberOfTimesProgrammed() {
        return numberOfTimesProgrammed;
    }

    public void setNumberOfTimesProgrammed(int numberOfTimesProgrammed) {
        this.numberOfTimesProgrammed = numberOfTimesProgrammed;
    }

    public int getManualTestMode() {
        return manualTestMode;
    }

    public void setManualTestMode(int manualTestMode) {
        this.manualTestMode = manualTestMode;
    }

    public int getOpticalTestMode() {
        return opticalTestMode;
    }

    public void setOpticalTestMode(int opticalTestMode) {
        this.opticalTestMode = opticalTestMode;
    }

    public Date getLastProgrammingTime() {
        return lastProgrammingTime;
    }

    public void setLastProgrammingTime(Date lastProgrammingTime) {
        this.lastProgrammingTime = lastProgrammingTime;
    }

    public long getBatteryCarryoverTime() {
        return batteryCarryoverTime;
    }

    public void setBatteryCarryoverTime(long batteryCarryoverTime) {
        this.batteryCarryoverTime = batteryCarryoverTime;
    }


}
