/*
 * ActualRegisterTable.java
 *
 * Created on 27 oktober 2005, 16:30
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
public class ActualRegisterTable extends AbstractTable {

    private int registerFunction1Bitfield;
    private boolean seasonInfoFieldFlag;
    private boolean dateTimeFieldFlag;
    private boolean demandResetControlFlag;
    private boolean demandResetLockFlag;
    private boolean cumulativeDemandFlag;
    private boolean continueCumulativeDemandFlag;
    private boolean timeRemainingFlag;

    private int registerFunction2Bitfield;
    private boolean selfReadInhibitOverflowFlag;
    private boolean selfReadSeqNrFlag;
    private boolean dailySelfReadFlag;
    private boolean weeklySelfReadFlag;
    private int selfReadDemandReset;


    private int nrOfSelfReads;
    private int nrOfSummations;
    private int nrOfDemands;
    private int nrOfCoinValues;
    private int nrOfOccur;
    private int nrOfTiers;
    private int nrOfPresentDemands;
    private int nrOfPresentValues;


    /** Creates a new instance of ActualRegisterTable */
    public ActualRegisterTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(21));
    }

    public String toString() {
        return "ActualRegisterTable: registerFunction1Bitfield=0x"+Integer.toHexString(getRegisterFunction1Bitfield())+
               ", registerFunction2Bitfield=0x"+Integer.toHexString(getRegisterFunction2Bitfield())+
               ", nrOfSelfReads="+getNrOfSelfReads()+
               ", nrOfSummations="+getNrOfSummations()+
               ", nrOfDemands="+getNrOfDemands()+
               ", nrOfCoinValues="+getNrOfCoinValues()+
               ", nrOfOccur="+getNrOfOccur()+
               ", nrOfTiers="+getNrOfTiers()+
               ", nrOfPresentDemands="+getNrOfPresentDemands()+
               ", nrOfPresentValues="+getNrOfPresentValues()+"\n";

    }

    protected void parse(byte[] tableData) throws IOException {
        registerFunction1Bitfield=C12ParseUtils.getInt(tableData,0);
        setSeasonInfoFieldFlag((registerFunction1Bitfield&0x01) == 0x01);
        setDateTimeFieldFlag((registerFunction1Bitfield&0x02) == 0x02);
        setDemandResetControlFlag((registerFunction1Bitfield&0x04) == 0x04);
        setDemandResetLockFlag((registerFunction1Bitfield&0x08) == 0x08);
        setCumulativeDemandFlag((registerFunction1Bitfield&0x10) == 0x10);
        setContinueCumulativeDemandFlag((registerFunction1Bitfield&0x20) == 0x20);
        setTimeRemainingFlag((registerFunction1Bitfield&0x40) == 0x40);
        registerFunction2Bitfield=C12ParseUtils.getInt(tableData,1);
        setSelfReadInhibitOverflowFlag((registerFunction2Bitfield&0x01) == 0x01);
        setSelfReadSeqNrFlag((registerFunction2Bitfield&0x02) == 0x02);
        setDailySelfReadFlag((registerFunction2Bitfield&0x04) == 0x04);
        setWeeklySelfReadFlag((registerFunction2Bitfield&0x08) == 0x08);
        setSelfReadDemandReset((registerFunction2Bitfield&0x30)>>4);
        nrOfSelfReads=C12ParseUtils.getInt(tableData,2);
        nrOfSummations=C12ParseUtils.getInt(tableData,3);
        nrOfDemands=C12ParseUtils.getInt(tableData,4);
        nrOfCoinValues=C12ParseUtils.getInt(tableData,5);
        nrOfOccur=C12ParseUtils.getInt(tableData,6);
        nrOfTiers=C12ParseUtils.getInt(tableData,7);
        nrOfPresentDemands=C12ParseUtils.getInt(tableData,8);
        nrOfPresentValues=C12ParseUtils.getInt(tableData,9);
    }

    public int getRegisterFunction1Bitfield() {
        return registerFunction1Bitfield;
    }

    public void setRegisterFunction1Bitfield(int registerFunction1Bitfield) {
        this.registerFunction1Bitfield = registerFunction1Bitfield;
    }

    public int getRegisterFunction2Bitfield() {
        return registerFunction2Bitfield;
    }

    public void setRegisterFunction2Bitfield(int registerFunction2Bitfield) {
        this.registerFunction2Bitfield = registerFunction2Bitfield;
    }

    public int getNrOfSelfReads() {
        return nrOfSelfReads;
    }

    public void setNrOfSelfReads(int nrOfSelfReads) {
        this.nrOfSelfReads = nrOfSelfReads;
    }

    public int getNrOfSummations() {
        return nrOfSummations;
    }

    public void setNrOfSummations(int nrOfSummations) {
        this.nrOfSummations = nrOfSummations;
    }

    public int getNrOfDemands() {
        return nrOfDemands;
    }

    public void setNrOfDemands(int nrOfDemands) {
        this.nrOfDemands = nrOfDemands;
    }

    public int getNrOfCoinValues() {
        return nrOfCoinValues;
    }

    public void setNrOfCoinValues(int nrOfCoinValues) {
        this.nrOfCoinValues = nrOfCoinValues;
    }

    public int getNrOfOccur() {
        return nrOfOccur;
    }

    public void setNrOfOccur(int nrOfOccur) {
        this.nrOfOccur = nrOfOccur;
    }

    public int getNrOfTiers() {
        return nrOfTiers;
    }

    public void setNrOfTiers(int nrOfTiers) {
        this.nrOfTiers = nrOfTiers;
    }

    public int getNrOfPresentDemands() {
        return nrOfPresentDemands;
    }

    public void setNrOfPresentDemands(int nrOfPresentDemands) {
        this.nrOfPresentDemands = nrOfPresentDemands;
    }

    public int getNrOfPresentValues() {
        return nrOfPresentValues;
    }

    public void setNrOfPresentValues(int nrOfPresentValues) {
        this.nrOfPresentValues = nrOfPresentValues;
    }

    public boolean isSeasonInfoFieldFlag() {
        return seasonInfoFieldFlag;
    }

    public void setSeasonInfoFieldFlag(boolean seasonInfoFieldFlag) {
        this.seasonInfoFieldFlag = seasonInfoFieldFlag;
    }

    public boolean isDateTimeFieldFlag() {
        return dateTimeFieldFlag;
    }

    public void setDateTimeFieldFlag(boolean dateTimeFieldFlag) {
        this.dateTimeFieldFlag = dateTimeFieldFlag;
    }

    public boolean isDemandResetControlFlag() {
        return demandResetControlFlag;
    }

    public void setDemandResetControlFlag(boolean demandResetControlFlag) {
        this.demandResetControlFlag = demandResetControlFlag;
    }

    public boolean isDemandResetLockFlag() {
        return demandResetLockFlag;
    }

    public void setDemandResetLockFlag(boolean demandResetLockFlag) {
        this.demandResetLockFlag = demandResetLockFlag;
    }

    public boolean isCumulativeDemandFlag() {
        return cumulativeDemandFlag;
    }

    public void setCumulativeDemandFlag(boolean cumulativeDemandFlag) {
        this.cumulativeDemandFlag = cumulativeDemandFlag;
    }

    public boolean isContinueCumulativeDemandFlag() {
        return continueCumulativeDemandFlag;
    }

    public void setContinueCumulativeDemandFlag(boolean continueCumulativeDemandFlag) {
        this.continueCumulativeDemandFlag = continueCumulativeDemandFlag;
    }

    public boolean isTimeRemainingFlag() {
        return timeRemainingFlag;
    }

    public void setTimeRemainingFlag(boolean timeRemainingFlag) {
        this.timeRemainingFlag = timeRemainingFlag;
    }

    public boolean isSelfReadInhibitOverflowFlag() {
        return selfReadInhibitOverflowFlag;
    }

    public void setSelfReadInhibitOverflowFlag(boolean selfReadInhibitOverflowFlag) {
        this.selfReadInhibitOverflowFlag = selfReadInhibitOverflowFlag;
    }

    public boolean isSelfReadSeqNrFlag() {
        return selfReadSeqNrFlag;
    }

    public void setSelfReadSeqNrFlag(boolean selfReadSeqNrFlag) {
        this.selfReadSeqNrFlag = selfReadSeqNrFlag;
    }

    public boolean isDailySelfReadFlag() {
        return dailySelfReadFlag;
    }

    public void setDailySelfReadFlag(boolean dailySelfReadFlag) {
        this.dailySelfReadFlag = dailySelfReadFlag;
    }

    public boolean isWeeklySelfReadFlag() {
        return weeklySelfReadFlag;
    }

    public void setWeeklySelfReadFlag(boolean weeklySelfReadFlag) {
        this.weeklySelfReadFlag = weeklySelfReadFlag;
    }

    public int getSelfReadDemandReset() {
        return selfReadDemandReset;
    }

    public void setSelfReadDemandReset(int selfReadDemandReset) {
        this.selfReadDemandReset = selfReadDemandReset;
    }

}
