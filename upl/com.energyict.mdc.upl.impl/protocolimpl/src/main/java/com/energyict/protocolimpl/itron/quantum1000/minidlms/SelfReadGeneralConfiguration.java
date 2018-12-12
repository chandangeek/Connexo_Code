/*
 * SelfReadGeneralConfiguration.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class SelfReadGeneralConfiguration extends AbstractDataDefinition {
    
    private SelfReadTriggerInfo[] selfReadTriggerInfos = new SelfReadTriggerInfo[24];
    
    private boolean freezeEveryNminutes; // BOOLEAN,
    private int numberOfMinutes; // UNSIGNED16,
    private int actionCode; // UNSIGNED8,
    private int synchTo; // UNSIGNED16,
    
    private boolean monthlyOnNthDay; // BOOLEAN,
    private int onDay; // UNSIGNED8, --1..31
    private Date atTimeOnDay; // TIME,
    private int actionCodeAtime; // UNSIGNED8,
    
    private boolean weekly; // BOOLEAN,
    private int actionCodeWeekly;// UNSIGNED8,
    private Date atTimeWeekly; // TIME,
    private int onDays; // UNSIGNED8, --Bit0 = Monday / Bit1 = Tuesday etc...
    
    private boolean digitalStateInput; // BOOLEAN,
    private int actionCodeDigitalStateInput; // UNSIGNED8,
    
    private boolean communicationCMD; // BOOLEAN,
    private int actionCodeCommunicationCMD; // UNSIGNED8,
    
    private boolean demandReset; // BOOLEAN,
    private int actionCodeDemandReset; // UNSIGNED8,
    
    private boolean afterSeasonChange; // BOOLEAN,
    private int actionCodeAfterSeasonChange; // UNSIGNED8,
    
    private boolean oneTestModeEntry; // BOOLEAN,
    private int actionCodeOneTestModeEntry; // UNSIGNED8,
    
    private boolean onTestModeExit; // BOOLEAN,
    private int actionCodeOnTestModeExit; // UNSIGNED8,
    
    private int numRecordsPerFile; // UNSIGNED16, --At least 1
    private boolean currentLatentSwitch; // BOOLEAN,
    private int actionCodeCurrentLatentSwitch; // UNSIGNED8,
    
    /** Creates a new instance of SelfReadGeneralConfiguration */
    public SelfReadGeneralConfiguration(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadGeneralConfiguration:\n");
        strBuff.append("   actionCode="+getActionCode()+"\n");
        strBuff.append("   actionCodeAfterSeasonChange="+getActionCodeAfterSeasonChange()+"\n");
        strBuff.append("   actionCodeAtime="+getActionCodeAtime()+"\n");
        strBuff.append("   actionCodeCommunicationCMD="+getActionCodeCommunicationCMD()+"\n");
        strBuff.append("   actionCodeCurrentLatentSwitch="+getActionCodeCurrentLatentSwitch()+"\n");
        strBuff.append("   actionCodeDemandReset="+getActionCodeDemandReset()+"\n");
        strBuff.append("   actionCodeDigitalStateInput="+getActionCodeDigitalStateInput()+"\n");
        strBuff.append("   actionCodeOnTestModeExit="+getActionCodeOnTestModeExit()+"\n");
        strBuff.append("   actionCodeOneTestModeEntry="+getActionCodeOneTestModeEntry()+"\n");
        strBuff.append("   actionCodeWeekly="+getActionCodeWeekly()+"\n");
        strBuff.append("   afterSeasonChange="+isAfterSeasonChange()+"\n");
        strBuff.append("   atTimeOnDay="+getAtTimeOnDay()+"\n");
        strBuff.append("   atTimeWeekly="+getAtTimeWeekly()+"\n");
        strBuff.append("   communicationCMD="+isCommunicationCMD()+"\n");
        strBuff.append("   currentLatentSwitch="+isCurrentLatentSwitch()+"\n");
        strBuff.append("   demandReset="+isDemandReset()+"\n");
        strBuff.append("   digitalStateInput="+isDigitalStateInput()+"\n");
        strBuff.append("   freezeEveryNminutes="+isFreezeEveryNminutes()+"\n");
        strBuff.append("   monthlyOnNthDay="+isMonthlyOnNthDay()+"\n");
        strBuff.append("   numRecordsPerFile="+getNumRecordsPerFile()+"\n");
        strBuff.append("   numberOfMinutes="+getNumberOfMinutes()+"\n");
        strBuff.append("   onDay="+getOnDay()+"\n");
        strBuff.append("   onDays="+getOnDays()+"\n");
        strBuff.append("   onTestModeExit="+isOnTestModeExit()+"\n");
        strBuff.append("   oneTestModeEntry="+isOneTestModeEntry()+"\n");
        for (int i=0;i<getSelfReadTriggerInfos().length;i++) {
            strBuff.append("       selfReadTriggerInfos["+i+"]="+getSelfReadTriggerInfos()[i]+"\n");
        }
        strBuff.append("   synchTo="+getSynchTo()+"\n");
        strBuff.append("   weekly="+isWeekly()+"\n");
        return strBuff.toString();
    }

    
    protected int getVariableName() {
        return 0x0046; // 70 DLMS_SELF_READ_GENERAL_CONFIG
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        
        for (int i=0;i<getSelfReadTriggerInfos().length;i++) {
            getSelfReadTriggerInfos()[i] = new SelfReadTriggerInfo(data,offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone());
            offset+=SelfReadTriggerInfo.size();
        }
        
        setFreezeEveryNminutes(ProtocolUtils.getInt(data,offset++,1)==1);
        setNumberOfMinutes(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setActionCode(ProtocolUtils.getInt(data,offset++,1));
        setSynchTo(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        
        setMonthlyOnNthDay(ProtocolUtils.getInt(data,offset++,1)==1);
        setOnDay(ProtocolUtils.getInt(data,offset++,1));
        setAtTimeOnDay(Utils.getDateFromTime(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getTimeSize();
        setActionCodeAtime(ProtocolUtils.getInt(data,offset++,1));
        
        setWeekly(ProtocolUtils.getInt(data,offset++,1)==1);
        setActionCodeWeekly(ProtocolUtils.getInt(data,offset++,1));
        setAtTimeWeekly(Utils.getDateFromTime(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getTimeSize();
        setOnDays(ProtocolUtils.getInt(data,offset++,1));
        
        setDigitalStateInput(ProtocolUtils.getInt(data,offset++,1)==1);
        setActionCodeDigitalStateInput(ProtocolUtils.getInt(data,offset++,1));
        
        setCommunicationCMD(ProtocolUtils.getInt(data,offset++,1)==1);
        setActionCodeCommunicationCMD(ProtocolUtils.getInt(data,offset++,1));
        
        setDemandReset(ProtocolUtils.getInt(data,offset++,1)==1);
        setActionCodeDemandReset(ProtocolUtils.getInt(data,offset++,1));
        
        setAfterSeasonChange(ProtocolUtils.getInt(data,offset++,1)==1);
        setActionCodeAfterSeasonChange(ProtocolUtils.getInt(data,offset++,1));
        
        setOneTestModeEntry(ProtocolUtils.getInt(data,offset++,1)==1);
        setActionCodeOneTestModeEntry(ProtocolUtils.getInt(data,offset++,1));
        
        setOnTestModeExit(ProtocolUtils.getInt(data,offset++,1)==1);
        setActionCodeOnTestModeExit(ProtocolUtils.getInt(data,offset++,1));
        
        setNumRecordsPerFile(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        if (data.length>224) {
            setCurrentLatentSwitch(ProtocolUtils.getInt(data,offset++,1)==1);
            setActionCodeCurrentLatentSwitch(ProtocolUtils.getInt(data,offset++,1));
        }
    }

    public SelfReadTriggerInfo[] getSelfReadTriggerInfos() {
        return selfReadTriggerInfos;
    }

    public void setSelfReadTriggerInfos(SelfReadTriggerInfo[] selfReadTriggerInfos) {
        this.selfReadTriggerInfos = selfReadTriggerInfos;
    }

    public boolean isFreezeEveryNminutes() {
        return freezeEveryNminutes;
    }

    public void setFreezeEveryNminutes(boolean freezeEveryNminutes) {
        this.freezeEveryNminutes = freezeEveryNminutes;
    }

    public int getNumberOfMinutes() {
        return numberOfMinutes;
    }

    public void setNumberOfMinutes(int numberOfMinutes) {
        this.numberOfMinutes = numberOfMinutes;
    }

    public int getActionCode() {
        return actionCode;
    }

    public void setActionCode(int actionCode) {
        this.actionCode = actionCode;
    }

    public int getSynchTo() {
        return synchTo;
    }

    public void setSynchTo(int synchTo) {
        this.synchTo = synchTo;
    }

    public boolean isMonthlyOnNthDay() {
        return monthlyOnNthDay;
    }

    public void setMonthlyOnNthDay(boolean monthlyOnNthDay) {
        this.monthlyOnNthDay = monthlyOnNthDay;
    }

    public int getOnDay() {
        return onDay;
    }

    public void setOnDay(int onDay) {
        this.onDay = onDay;
    }

    public Date getAtTimeOnDay() {
        return atTimeOnDay;
    }

    public void setAtTimeOnDay(Date atTimeOnDay) {
        this.atTimeOnDay = atTimeOnDay;
    }

    public int getActionCodeAtime() {
        return actionCodeAtime;
    }

    public void setActionCodeAtime(int actionCodeAtime) {
        this.actionCodeAtime = actionCodeAtime;
    }

    public boolean isWeekly() {
        return weekly;
    }

    public void setWeekly(boolean weekly) {
        this.weekly = weekly;
    }

    public int getActionCodeWeekly() {
        return actionCodeWeekly;
    }

    public void setActionCodeWeekly(int actionCodeWeekly) {
        this.actionCodeWeekly = actionCodeWeekly;
    }

    public Date getAtTimeWeekly() {
        return atTimeWeekly;
    }

    public void setAtTimeWeekly(Date atTimeWeekly) {
        this.atTimeWeekly = atTimeWeekly;
    }

    public int getOnDays() {
        return onDays;
    }

    public void setOnDays(int onDays) {
        this.onDays = onDays;
    }

    public boolean isDigitalStateInput() {
        return digitalStateInput;
    }

    public void setDigitalStateInput(boolean digitalStateInput) {
        this.digitalStateInput = digitalStateInput;
    }

    public int getActionCodeDigitalStateInput() {
        return actionCodeDigitalStateInput;
    }

    public void setActionCodeDigitalStateInput(int actionCodeDigitalStateInput) {
        this.actionCodeDigitalStateInput = actionCodeDigitalStateInput;
    }

    public boolean isCommunicationCMD() {
        return communicationCMD;
    }

    public void setCommunicationCMD(boolean communicationCMD) {
        this.communicationCMD = communicationCMD;
    }

    public int getActionCodeCommunicationCMD() {
        return actionCodeCommunicationCMD;
    }

    public void setActionCodeCommunicationCMD(int actionCodeCommunicationCMD) {
        this.actionCodeCommunicationCMD = actionCodeCommunicationCMD;
    }

    public boolean isDemandReset() {
        return demandReset;
    }

    public void setDemandReset(boolean demandReset) {
        this.demandReset = demandReset;
    }

    public int getActionCodeDemandReset() {
        return actionCodeDemandReset;
    }

    public void setActionCodeDemandReset(int actionCodeDemandReset) {
        this.actionCodeDemandReset = actionCodeDemandReset;
    }

    public boolean isAfterSeasonChange() {
        return afterSeasonChange;
    }

    public void setAfterSeasonChange(boolean afterSeasonChange) {
        this.afterSeasonChange = afterSeasonChange;
    }

    public int getActionCodeAfterSeasonChange() {
        return actionCodeAfterSeasonChange;
    }

    public void setActionCodeAfterSeasonChange(int actionCodeAfterSeasonChange) {
        this.actionCodeAfterSeasonChange = actionCodeAfterSeasonChange;
    }

    public boolean isOneTestModeEntry() {
        return oneTestModeEntry;
    }

    public void setOneTestModeEntry(boolean oneTestModeEntry) {
        this.oneTestModeEntry = oneTestModeEntry;
    }

    public int getActionCodeOneTestModeEntry() {
        return actionCodeOneTestModeEntry;
    }

    public void setActionCodeOneTestModeEntry(int actionCodeOneTestModeEntry) {
        this.actionCodeOneTestModeEntry = actionCodeOneTestModeEntry;
    }

    public boolean isOnTestModeExit() {
        return onTestModeExit;
    }

    public void setOnTestModeExit(boolean onTestModeExit) {
        this.onTestModeExit = onTestModeExit;
    }

    public int getActionCodeOnTestModeExit() {
        return actionCodeOnTestModeExit;
    }

    public void setActionCodeOnTestModeExit(int actionCodeOnTestModeExit) {
        this.actionCodeOnTestModeExit = actionCodeOnTestModeExit;
    }

    public int getNumRecordsPerFile() {
        return numRecordsPerFile;
    }

    public void setNumRecordsPerFile(int numRecordsPerFile) {
        this.numRecordsPerFile = numRecordsPerFile;
    }

    public boolean isCurrentLatentSwitch() {
        return currentLatentSwitch;
    }

    public void setCurrentLatentSwitch(boolean currentLatentSwitch) {
        this.currentLatentSwitch = currentLatentSwitch;
    }

    public int getActionCodeCurrentLatentSwitch() {
        return actionCodeCurrentLatentSwitch;
    }

    public void setActionCodeCurrentLatentSwitch(int actionCodeCurrentLatentSwitch) {
        this.actionCodeCurrentLatentSwitch = actionCodeCurrentLatentSwitch;
    }
}
