/*
 * TimeTOU.java
 *
 * Created on 2 november 2005, 9:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TimeTOU {


    private int timeFunctionFlagBitfield1; // 8 bit
    private boolean touSelfReadFlag; // bit 0 : tou self read flag
    private boolean seasonSelfReadFlag; // bit 1 : season self read flag
    private boolean seasonDemandResetflag; // bit 2 : season demand reset flag
    private boolean seasonChangeArmedFlag; // bit 3 : season chng armed flag
    private boolean sortDatesFlag; // bit 4 : sort dates flag
    private boolean anchorDateFlag; // bit 5 : anchor date flag

    private int timeFunctionFlagBitfield2; // 8 bit
    private boolean capabilityDSTSwitchAutoFlag; // bit 0 : cap dst auto flag
    private boolean separateWeekdaysFlag; // bit 1 : separate weekdays flag
    private boolean separateSumDemandsFlag; // bit 2 : separate sum demands flag
    private boolean sortTierSwitchesFlag; // bit 3 : sort tier switches flag
    private boolean timeZoneOffsetCapability; // bit 4 : cap tm zn offset flag

    private int calendarBitfield; // 8 bit
    private int nrOfSeasons; // bit 3..0 : number of seasons
    private int nrOfSpecialSchedules; // bit 7..4 : number of special schedules

    private int nrOfNonRecurringDates; // 8 bit
    private int nrOfRecurringDates; // 8 bit
    private int nrOfTierSwitches; // 16 bit
    private int calendarTableSize; // 16 bit

    /** Creates a new instance of TimeTOU */
    public TimeTOU(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setTimeFunctionFlagBitfield1(C12ParseUtils.getInt(data,offset++));
        setTouSelfReadFlag((getTimeFunctionFlagBitfield1() & 0x01) == 0x01);
        setSeasonSelfReadFlag((getTimeFunctionFlagBitfield1() & 0x02) == 0x02);
        setSeasonDemandResetflag((getTimeFunctionFlagBitfield1() & 0x04) == 0x04);
        setSeasonChangeArmedFlag((getTimeFunctionFlagBitfield1() & 0x08) == 0x08);
        setSortDatesFlag((getTimeFunctionFlagBitfield1() & 0x10) == 0x10);
        setAnchorDateFlag((getTimeFunctionFlagBitfield1() & 0x20) == 0x20);

        setTimeFunctionFlagBitfield2(C12ParseUtils.getInt(data,offset++));
        setCapabilityDSTSwitchAutoFlag((getTimeFunctionFlagBitfield2() & 0x01)==0x01);
        setSeparateWeekdaysFlag((getTimeFunctionFlagBitfield2() & 0x02)==0x02);
        setSeparateSumDemandsFlag((getTimeFunctionFlagBitfield2() & 0x04)==0x04);
        setSortTierSwitchesFlag((getTimeFunctionFlagBitfield2() & 0x08)==0x08);
        setTimeZoneOffsetCapability((getTimeFunctionFlagBitfield2() & 0x10)==0x10);

        setCalendarBitfield(C12ParseUtils.getInt(data,offset++));
        setNrOfSeasons(getCalendarBitfield() & 0x0F);
        setNrOfSpecialSchedules((getCalendarBitfield() >> 4) & 0xF0);

        setNrOfNonRecurringDates(C12ParseUtils.getInt(data,offset++));
        setNrOfRecurringDates(C12ParseUtils.getInt(data,offset++));
        setNrOfTierSwitches(C12ParseUtils.getInt(data,offset,2,dataOrder));
        offset+=2;
        setCalendarTableSize(C12ParseUtils.getInt(data,offset,2,dataOrder));
        offset+=2;
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TimeTOU: timeFunctionFlagBitfield1=0x"+Integer.toHexString(getTimeFunctionFlagBitfield1())+", timeFunctionFlagBitfield2=0x"+Integer.toHexString(getTimeFunctionFlagBitfield2())+", calendarBitfield=0x"+Integer.toHexString(getCalendarBitfield())+", nrOfSeasons="+getNrOfSeasons()+", nrOfSpecialSchedules="+getNrOfSpecialSchedules()+", nrOfNonRecurringDates="+getNrOfNonRecurringDates()+", nrOfRecurringDates="+getNrOfRecurringDates()+", nrOfTierSwitches="+getNrOfTierSwitches()+", calendarTableSize="+getCalendarTableSize()+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return  9;
    }

    public int getTimeFunctionFlagBitfield1() {
        return timeFunctionFlagBitfield1;
    }

    public void setTimeFunctionFlagBitfield1(int timeFunctionFlagBitfield1) {
        this.timeFunctionFlagBitfield1 = timeFunctionFlagBitfield1;
    }

    public boolean isTouSelfReadFlag() {
        return touSelfReadFlag;
    }

    public void setTouSelfReadFlag(boolean touSelfReadFlag) {
        this.touSelfReadFlag = touSelfReadFlag;
    }

    public boolean isSeasonSelfReadFlag() {
        return seasonSelfReadFlag;
    }

    public void setSeasonSelfReadFlag(boolean seasonSelfReadFlag) {
        this.seasonSelfReadFlag = seasonSelfReadFlag;
    }

    public boolean isSeasonDemandResetflag() {
        return seasonDemandResetflag;
    }

    public void setSeasonDemandResetflag(boolean seasonDemandResetflag) {
        this.seasonDemandResetflag = seasonDemandResetflag;
    }

    public boolean isSeasonChangeArmedFlag() {
        return seasonChangeArmedFlag;
    }

    public void setSeasonChangeArmedFlag(boolean seasonChangeArmedFlag) {
        this.seasonChangeArmedFlag = seasonChangeArmedFlag;
    }

    public boolean isSortDatesFlag() {
        return sortDatesFlag;
    }

    public void setSortDatesFlag(boolean sortDatesFlag) {
        this.sortDatesFlag = sortDatesFlag;
    }

    public boolean isAnchorDateFlag() {
        return anchorDateFlag;
    }

    public void setAnchorDateFlag(boolean anchorDateFlag) {
        this.anchorDateFlag = anchorDateFlag;
    }

    public int getTimeFunctionFlagBitfield2() {
        return timeFunctionFlagBitfield2;
    }

    public void setTimeFunctionFlagBitfield2(int timeFunctionFlagBitfield2) {
        this.timeFunctionFlagBitfield2 = timeFunctionFlagBitfield2;
    }

    public boolean isCapabilityDSTSwitchAutoFlag() {
        return capabilityDSTSwitchAutoFlag;
    }

    public void setCapabilityDSTSwitchAutoFlag(boolean capabilityDSTSwitchAutoFlag) {
        this.capabilityDSTSwitchAutoFlag = capabilityDSTSwitchAutoFlag;
    }

    public boolean isSeparateWeekdaysFlag() {
        return separateWeekdaysFlag;
    }

    public void setSeparateWeekdaysFlag(boolean separateWeekdaysFlag) {
        this.separateWeekdaysFlag = separateWeekdaysFlag;
    }

    public boolean isSeparateSumDemandsFlag() {
        return separateSumDemandsFlag;
    }

    public void setSeparateSumDemandsFlag(boolean separateSumDemandsFlag) {
        this.separateSumDemandsFlag = separateSumDemandsFlag;
    }

    public boolean isSortTierSwitchesFlag() {
        return sortTierSwitchesFlag;
    }

    public void setSortTierSwitchesFlag(boolean sortTierSwitchesFlag) {
        this.sortTierSwitchesFlag = sortTierSwitchesFlag;
    }

    public boolean isTimeZoneOffsetCapability() {
        return timeZoneOffsetCapability;
    }

    public void setTimeZoneOffsetCapability(boolean timeZoneOffsetCapability) {
        this.timeZoneOffsetCapability = timeZoneOffsetCapability;
    }

    public int getCalendarBitfield() {
        return calendarBitfield;
    }

    public void setCalendarBitfield(int calendarBitfield) {
        this.calendarBitfield = calendarBitfield;
    }

    public int getNrOfSeasons() {
        return nrOfSeasons;
    }

    public void setNrOfSeasons(int nrOfSeasons) {
        this.nrOfSeasons = nrOfSeasons;
    }

    public int getNrOfSpecialSchedules() {
        return nrOfSpecialSchedules;
    }

    public void setNrOfSpecialSchedules(int nrOfSpecialSchedules) {
        this.nrOfSpecialSchedules = nrOfSpecialSchedules;
    }

    public int getNrOfNonRecurringDates() {
        return nrOfNonRecurringDates;
    }

    public void setNrOfNonRecurringDates(int nrOfNonRecurringDates) {
        this.nrOfNonRecurringDates = nrOfNonRecurringDates;
    }

    public int getNrOfRecurringDates() {
        return nrOfRecurringDates;
    }

    public void setNrOfRecurringDates(int nrOfRecurringDates) {
        this.nrOfRecurringDates = nrOfRecurringDates;
    }

    public int getNrOfTierSwitches() {
        return nrOfTierSwitches;
    }

    public void setNrOfTierSwitches(int nrOfTierSwitches) {
        this.nrOfTierSwitches = nrOfTierSwitches;
    }

    public int getCalendarTableSize() {
        return calendarTableSize;
    }

    public void setCalendarTableSize(int calendarTableSize) {
        this.calendarTableSize = calendarTableSize;
    }
}
