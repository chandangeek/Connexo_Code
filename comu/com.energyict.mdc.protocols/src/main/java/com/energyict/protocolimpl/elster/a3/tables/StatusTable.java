/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * StatusTable.java
 *
 * Created on 10 februari 2006, 16:49
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class StatusTable extends AbstractTable {

/*
    Memory storage: EEPROM
    Total table size: (bytes) 89, Fixed
    Read access: 1
    Write access: Not allowed
    ST-3 is used for status flags. MT-3 contains the remaining statuses:
*/

    private byte[] configurationErrors;	// 7 bytes For definition of what causes configuration errors refer to the associated table.
    /*
          b0:	ST11_CONFIG_ERROR
                    b1:	ST13_CONFIG_ERROR
                    b2:	ST15_CONFIG_ERROR
                    b3:	MT10_CONFIG_ERROR
                    b4:	MT13_CONFIG_ERROR
                    b5:	MT18_CONFIG_ERROR
                    b6:	TARIFF_CONFIG_ERROR
                    b7:	PRESDMDS_CONFIG_ERROR
                    b8:	PRESVAL_CONFIG_ERROR
                    b9:	ST33_CONFIG_ERROR
                    b10:	MT15_CONFIG_ERROR
                    b11:  MT21_CONFIG_ERROR
                    b12:  ST26_ALLOCATION_ERROR
                    b13:  KEY_CHANGE_ERROR
                    b14:  ST75_ CONFIG_ERROR
                    b15:  TOU_CONFIG_ERROR
                    b16:  CLOCK_CONFIG_ERROR
                    b17:  DSP_Config_Error
                    b18:  Instrumentation_Config_Error
                    b19:  Pending_ST13_Config_Error
                    b20:  Pending_ST33_Config_Error
                    b21:  Display_Config_Error
                    b22:  ST94_Config_Error
                    b23:  MT94_Config_Error
                    b24:  ST54_Config_Error
                    b25:  MT29_Config_Error
                    b26:  Pending ST-54 Config Error
                    b27:  PQM_CONFIG_ERROR
                    b28:  ST76_ALLOCATION_ERROR
                    b29:  ST64_ALLOCATION_ERROR
                    b30:  MT64_ALLOCATION_ERROR
                    b31:  MT65_ALLOCATION_ERROR
                    b32:  MT49_ALLOCATION_ERROR
                    b33:  SERVICE_VOLTAGE_CONFIG_ERROR
                    b34:  LP_PULSE_SET1_CONFIG_ERROR
                    b35:  LP_INSTR_SET1_CONFIG_ERROR
                    b36:  LP_INSTR_SET2_CONFIG_ERROR
                    b37:  ST92_CONFIG_ERROR
                    b38:  MT92_CONFIG_ERROR
                    b39:  ST93_CONFIG_ERROR
                    b40:  MT93_CONFIG_ERROR
                    b41:  ST95_CONFIG_ERROR
                    b42:  MT95_CONFIG_ERROR
                    b43:  MT90_CONFIG_ERROR
                    b44:  ST84_CONFIG_ERROR
                    b45:  ST85_CONFIG_ERROR
                    b46:  ST86_CONFIG_ERROR
                    b47:  ST87_CONFIG_ERROR
                    b48:  ST88_CONFIG_ERROR
                    b49:  ST89_CONFIG_ERROR
                    b50:  ST44_CONFIG_ERROR
                    b51:  ST74_ALLOCATION_ERROR
                    b52:  MT53_ALLOCATION_ERROR
                    b53-55: To Be Defined
    */
    private byte[] tableCRCErrorsCurrent; // 8 bytes There is a bit for each configuration table listed in MT-98. The CRC error bit is State)      set if the meter detects a table CRC error. Configuration tables have CRCs that are set by the meter when the table is written and checked when Health is run. If there are any table CRC errors, ST3.CurrentStateLatchedErrors.TABLE_CRC_ERROR will be set.
    private byte[] tableCRCErrorsLatched; // 8 bytes (EE) Latched state of table CRC errors State)
    private int latchedResourceErrors; // 1 (EE) Refer to ST3.Current State Resource_Errors
    private int latchedErrors; // 2 bytes (EE) refer to ST3.Current State Latched_Errors
    private int latchedWarnings; // 3 bytes (EE) refer to ST3.Current State Latched_Warnings
    private long latchedPQMWarnings; // 4 bytes Latched state of PQM tests. Bit 0 corresponds to PQM test 1 and bit 31 corresponds to PQM test 32.
    private Date oldTimeWhenTimeChanged; // 6 bytes (EE) YY,MM,DD,HH,MM,SS
    private Date newTimeWhenTimeChanged; // 6 bytes (EE) YY,MM,DD,HH,MM,SS
    //Comm Status      2 * 11 One entry for each uart.
    private Date comm1StatusDateTimeOfLastTableWrite; // 6 bytes
    private int comm1NumberOfLogons; // 2 bytes
    private int comm1NumberOfWriteSessions; // 2 bytes
    private int comm1NumberOfSecurityFailures; // 1 byte
    private Date comm2StatusDateTimeOfLastTableWrite; // 6 bytes
    private int comm2NumberOfLogons; // 2 bytes
    private int comm2NumberOfWriteSessions; // 2 bytes
    private int comm2NumberOfSecurityFailures; // 1 byte
    private int numberOfManualDemandResets; // 2 bytes (EE) Total number of button press and communication demand resets. Note that Resets      ST-23 records the total number of all demand resets, which includes calendar triggered demand resets.
    private int demandResetMechanism; // 1 byte (EE) The mechanism or trigger of the last demand reset: 0 = Demand reset has not occurred, 1 = Button press 2 = Communication Procedure 3 = Calendar (ST-54 or pending table switch) 4 = Forced by Max_Days_Between_Dmd_Resets feature.
    private int daysSinceDemandReset; // 1 byte (EE) Number of midnight crossings since the last demand reset.
    private int daysSincePulse; // 1 byte (EE) Number of midnight crossings since the last pulse was received.
    private Date powerFailTime; // 6 bytes YY,MM,DD,HH,MM,SS. Time of the last power failure.
    private Date powerRestorationTime; // 6 bytes YY,MM,DD,HH,MM,SS. Last power restoration time.
    private int cumulativePowerOutages; // 1 byte (EE) Total number of power outages.
    private long cumulativePowerOutageTime; // 4 bytes (EE) Cumulative power outage time in seconds.



    /** Creates a new instance of ElectricitySpecificProductSpec */
    public StatusTable(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(3,true));
    }


    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("StatusTable:\n");
        strBuff.append("   comm1NumberOfLogons="+getComm1NumberOfLogons()+"\n");
        strBuff.append("   comm1NumberOfSecurityFailures="+getComm1NumberOfSecurityFailures()+"\n");
        strBuff.append("   comm1NumberOfWriteSessions="+getComm1NumberOfWriteSessions()+"\n");
        strBuff.append("   comm1StatusDateTimeOfLastTableWrite="+getComm1StatusDateTimeOfLastTableWrite()+"\n");
        strBuff.append("   comm2NumberOfLogons="+getComm2NumberOfLogons()+"\n");
        strBuff.append("   comm2NumberOfSecurityFailures="+getComm2NumberOfSecurityFailures()+"\n");
        strBuff.append("   comm2NumberOfWriteSessions="+getComm2NumberOfWriteSessions()+"\n");
        strBuff.append("   comm2StatusDateTimeOfLastTableWrite="+getComm2StatusDateTimeOfLastTableWrite()+"\n");
        strBuff.append("   configurationErrors="+getConfigurationErrors()+"\n");
        strBuff.append("   cumulativePowerOutageTime="+getCumulativePowerOutageTime()+"\n");
        strBuff.append("   cumulativePowerOutages="+getCumulativePowerOutages()+"\n");
        strBuff.append("   daysSinceDemandReset="+getDaysSinceDemandReset()+"\n");
        strBuff.append("   daysSincePulse="+getDaysSincePulse()+"\n");
        strBuff.append("   demandResetMechanism="+getDemandResetMechanism()+"\n");
        strBuff.append("   latchedErrors="+getLatchedErrors()+"\n");
        strBuff.append("   latchedPQMWarnings="+getLatchedPQMWarnings()+"\n");
        strBuff.append("   latchedResourceErrors="+getLatchedResourceErrors()+"\n");
        strBuff.append("   latchedWarnings="+getLatchedWarnings()+"\n");
        strBuff.append("   newTimeWhenTimeChanged="+getNewTimeWhenTimeChanged()+"\n");
        strBuff.append("   numberOfManualDemandResets="+getNumberOfManualDemandResets()+"\n");
        strBuff.append("   oldTimeWhenTimeChanged="+getOldTimeWhenTimeChanged()+"\n");
        strBuff.append("   powerFailTime="+getPowerFailTime()+"\n");
        strBuff.append("   powerRestorationTime="+getPowerRestorationTime()+"\n");
        strBuff.append("   tableCRCErrorsCurrent="+getTableCRCErrorsCurrent()+"\n");
        strBuff.append("   tableCRCErrorsLatched="+getTableCRCErrorsLatched()+"\n");
        return strBuff.toString();
    }


    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setConfigurationErrors(ProtocolUtils.getSubArray2(tableData, offset, 7)); offset+=7;
        setTableCRCErrorsCurrent(ProtocolUtils.getSubArray2(tableData, offset, 8)); offset+=8;
        setTableCRCErrorsLatched(ProtocolUtils.getSubArray2(tableData, offset, 8)); offset+=8;
        setLatchedResourceErrors(C12ParseUtils.getInt(tableData,offset++));
        setLatchedErrors(C12ParseUtils.getInt(tableData,offset,2,dataOrder)); offset+=2;
        setLatchedWarnings(C12ParseUtils.getInt(tableData,offset,3,dataOrder)); offset+=3;
        setLatchedPQMWarnings(C12ParseUtils.getLong(tableData,offset,4,dataOrder)); offset+=4;
        setOldTimeWhenTimeChanged(C12ParseUtils.getDateFromLTime(tableData,offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        offset += C12ParseUtils.getLTimeSize(getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat());
        setNewTimeWhenTimeChanged(C12ParseUtils.getDateFromLTime(tableData,offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        offset += C12ParseUtils.getLTimeSize(getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat());
        setComm1StatusDateTimeOfLastTableWrite(C12ParseUtils.getDateFromLTime(tableData,offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        offset += C12ParseUtils.getLTimeSize(getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat());
        setComm1NumberOfLogons(C12ParseUtils.getInt(tableData,offset,2,dataOrder)); offset+=2;
        setComm1NumberOfWriteSessions(C12ParseUtils.getInt(tableData,offset,2,dataOrder)); offset+=2;
        setComm1NumberOfSecurityFailures(C12ParseUtils.getInt(tableData,offset++));
        setComm2StatusDateTimeOfLastTableWrite(C12ParseUtils.getDateFromLTime(tableData,offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        offset += C12ParseUtils.getLTimeSize(getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat());
        setComm2NumberOfLogons(C12ParseUtils.getInt(tableData,offset,2,dataOrder)); offset+=2;
        setComm2NumberOfWriteSessions(C12ParseUtils.getInt(tableData,offset,2,dataOrder)); offset+=2;
        setComm2NumberOfSecurityFailures(C12ParseUtils.getInt(tableData,offset++));
        setNumberOfManualDemandResets(C12ParseUtils.getInt(tableData,offset,2,dataOrder)); offset+=2;
        setDemandResetMechanism(C12ParseUtils.getInt(tableData,offset++));
        setDaysSinceDemandReset(C12ParseUtils.getInt(tableData,offset++));
        setDaysSincePulse(C12ParseUtils.getInt(tableData,offset++));
        setPowerFailTime(C12ParseUtils.getDateFromLTime(tableData,offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        offset += C12ParseUtils.getLTimeSize(getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat());
        setPowerRestorationTime(C12ParseUtils.getDateFromLTime(tableData,offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        offset += C12ParseUtils.getLTimeSize(getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat());
        setCumulativePowerOutages(C12ParseUtils.getInt(tableData,offset++));
        setCumulativePowerOutageTime(C12ParseUtils.getLong(tableData,offset,4,dataOrder)); offset+=4;
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    public byte[] getConfigurationErrors() {
        return configurationErrors;
    }

    public void setConfigurationErrors(byte[] configurationErrors) {
        this.configurationErrors = configurationErrors;
    }

    public byte[] getTableCRCErrorsCurrent() {
        return tableCRCErrorsCurrent;
    }

    public void setTableCRCErrorsCurrent(byte[] tableCRCErrorsCurrent) {
        this.tableCRCErrorsCurrent = tableCRCErrorsCurrent;
    }

    public byte[] getTableCRCErrorsLatched() {
        return tableCRCErrorsLatched;
    }

    public void setTableCRCErrorsLatched(byte[] tableCRCErrorsLatched) {
        this.tableCRCErrorsLatched = tableCRCErrorsLatched;
    }

    public int getLatchedResourceErrors() {
        return latchedResourceErrors;
    }

    public void setLatchedResourceErrors(int latchedResourceErrors) {
        this.latchedResourceErrors = latchedResourceErrors;
    }

    public int getLatchedErrors() {
        return latchedErrors;
    }

    public void setLatchedErrors(int latchedErrors) {
        this.latchedErrors = latchedErrors;
    }

    public int getLatchedWarnings() {
        return latchedWarnings;
    }

    public void setLatchedWarnings(int latchedWarnings) {
        this.latchedWarnings = latchedWarnings;
    }

    public long getLatchedPQMWarnings() {
        return latchedPQMWarnings;
    }

    public void setLatchedPQMWarnings(long latchedPQMWarnings) {
        this.latchedPQMWarnings = latchedPQMWarnings;
    }

    public Date getOldTimeWhenTimeChanged() {
        return oldTimeWhenTimeChanged;
    }

    public void setOldTimeWhenTimeChanged(Date oldTimeWhenTimeChanged) {
        this.oldTimeWhenTimeChanged = oldTimeWhenTimeChanged;
    }

    public Date getNewTimeWhenTimeChanged() {
        return newTimeWhenTimeChanged;
    }

    public void setNewTimeWhenTimeChanged(Date newTimeWhenTimeChanged) {
        this.newTimeWhenTimeChanged = newTimeWhenTimeChanged;
    }

    public Date getComm1StatusDateTimeOfLastTableWrite() {
        return comm1StatusDateTimeOfLastTableWrite;
    }

    public void setComm1StatusDateTimeOfLastTableWrite(Date comm1StatusDateTimeOfLastTableWrite) {
        this.comm1StatusDateTimeOfLastTableWrite = comm1StatusDateTimeOfLastTableWrite;
    }

    public int getComm1NumberOfLogons() {
        return comm1NumberOfLogons;
    }

    public void setComm1NumberOfLogons(int comm1NumberOfLogons) {
        this.comm1NumberOfLogons = comm1NumberOfLogons;
    }

    public int getComm1NumberOfWriteSessions() {
        return comm1NumberOfWriteSessions;
    }

    public void setComm1NumberOfWriteSessions(int comm1NumberOfWriteSessions) {
        this.comm1NumberOfWriteSessions = comm1NumberOfWriteSessions;
    }

    public int getComm1NumberOfSecurityFailures() {
        return comm1NumberOfSecurityFailures;
    }

    public void setComm1NumberOfSecurityFailures(int comm1NumberOfSecurityFailures) {
        this.comm1NumberOfSecurityFailures = comm1NumberOfSecurityFailures;
    }

    public Date getComm2StatusDateTimeOfLastTableWrite() {
        return comm2StatusDateTimeOfLastTableWrite;
    }

    public void setComm2StatusDateTimeOfLastTableWrite(Date comm2StatusDateTimeOfLastTableWrite) {
        this.comm2StatusDateTimeOfLastTableWrite = comm2StatusDateTimeOfLastTableWrite;
    }

    public int getComm2NumberOfLogons() {
        return comm2NumberOfLogons;
    }

    public void setComm2NumberOfLogons(int comm2NumberOfLogons) {
        this.comm2NumberOfLogons = comm2NumberOfLogons;
    }

    public int getComm2NumberOfWriteSessions() {
        return comm2NumberOfWriteSessions;
    }

    public void setComm2NumberOfWriteSessions(int comm2NumberOfWriteSessions) {
        this.comm2NumberOfWriteSessions = comm2NumberOfWriteSessions;
    }

    public int getComm2NumberOfSecurityFailures() {
        return comm2NumberOfSecurityFailures;
    }

    public void setComm2NumberOfSecurityFailures(int comm2NumberOfSecurityFailures) {
        this.comm2NumberOfSecurityFailures = comm2NumberOfSecurityFailures;
    }

    public int getNumberOfManualDemandResets() {
        return numberOfManualDemandResets;
    }

    public void setNumberOfManualDemandResets(int numberOfManualDemandResets) {
        this.numberOfManualDemandResets = numberOfManualDemandResets;
    }

    public int getDemandResetMechanism() {
        return demandResetMechanism;
    }

    public void setDemandResetMechanism(int demandResetMechanism) {
        this.demandResetMechanism = demandResetMechanism;
    }

    public int getDaysSinceDemandReset() {
        return daysSinceDemandReset;
    }

    public void setDaysSinceDemandReset(int daysSinceDemandReset) {
        this.daysSinceDemandReset = daysSinceDemandReset;
    }

    public int getDaysSincePulse() {
        return daysSincePulse;
    }

    public void setDaysSincePulse(int daysSincePulse) {
        this.daysSincePulse = daysSincePulse;
    }

    public Date getPowerFailTime() {
        return powerFailTime;
    }

    public void setPowerFailTime(Date powerFailTime) {
        this.powerFailTime = powerFailTime;
    }

    public Date getPowerRestorationTime() {
        return powerRestorationTime;
    }

    public void setPowerRestorationTime(Date powerRestorationTime) {
        this.powerRestorationTime = powerRestorationTime;
    }

    public int getCumulativePowerOutages() {
        return cumulativePowerOutages;
    }

    public void setCumulativePowerOutages(int cumulativePowerOutages) {
        this.cumulativePowerOutages = cumulativePowerOutages;
    }

    public long getCumulativePowerOutageTime() {
        return cumulativePowerOutageTime;
    }

    public void setCumulativePowerOutageTime(long cumulativePowerOutageTime) {
        this.cumulativePowerOutageTime = cumulativePowerOutageTime;
    }

}
