/*
 * ClockStateTable.java
 *
 * Created on 4 november 2005, 16:49
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class ClockStateTable extends AbstractTable {
    private Date clockCalendar;

    private TimeDateQualifier timeDateQualifier;

    private int statusBitfield; // 16 bit
    private int currSummTier; // bit 0..2
    private int currDemandTier; // bit 3..5
    private int currTier; // bit 0..2
    private int tierDrive; // bit 6..7
    private int specialScheduleActive; // bit 8..11
    private int season; // 12..15

    /** Creates a new instance of ClockStateTable */
    public ClockStateTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(55));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ClockStateTable: clockCalendar="+getClockCalendar()+", timeDateQualifier="+getTimeDateQualifier()+" statusBitfield="+Integer.toHexString(getStatusBitfield())+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        int offset=0;
        if (getTableFactory().getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
            setClockCalendar(C12ParseUtils.getDateFromLTimeAndAdjustForTimeZone(tableData, offset, cfgt.getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        else
            setClockCalendar(C12ParseUtils.getDateFromLTime(tableData, offset, cfgt.getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));

        offset+=C12ParseUtils.getLTimeSize(cfgt.getTimeFormat());
        setTimeDateQualifier(new TimeDateQualifier(tableData, offset, getTableFactory()));
        offset++;
        setStatusBitfield(C12ParseUtils.getInt(tableData,offset));
    }

    public Date getClockCalendar() {
        return clockCalendar;
    }

    public void setClockCalendar(Date clockCalendar) {
        this.clockCalendar = clockCalendar;
    }

    public TimeDateQualifier getTimeDateQualifier() {
        return timeDateQualifier;
    }

    public void setTimeDateQualifier(TimeDateQualifier timeDateQualifier) {
        this.timeDateQualifier = timeDateQualifier;
    }

    public int getStatusBitfield() {
        return statusBitfield;
    }

    public void setStatusBitfield(int statusBitfield) {
        this.statusBitfield = statusBitfield;
    }

    public int getCurrSummTier() {
        return currSummTier;
    }

    public void setCurrSummTier(int currSummTier) {
        this.currSummTier = currSummTier;
    }

    public int getCurrDemandTier() {
        return currDemandTier;
    }

    public void setCurrDemandTier(int currDemandTier) {
        this.currDemandTier = currDemandTier;
    }

    public int getCurrTier() {
        return currTier;
    }

    public void setCurrTier(int currTier) {
        this.currTier = currTier;
    }

    public int getTierDrive() {
        return tierDrive;
    }

    public void setTierDrive(int tierDrive) {
        this.tierDrive = tierDrive;
    }

    public int getSpecialScheduleActive() {
        return specialScheduleActive;
    }

    public void setSpecialScheduleActive(int specialScheduleActive) {
        this.specialScheduleActive = specialScheduleActive;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }
}
