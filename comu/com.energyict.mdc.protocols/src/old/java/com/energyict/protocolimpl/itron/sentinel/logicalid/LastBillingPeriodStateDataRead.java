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
import java.util.Date;

/**
 *
 * @author Koen
 */
public class LastBillingPeriodStateDataRead extends AbstractDataRead {

    // All FW versions
    private int lastBillingPeriodDemandResetCount; // UINT16
    private int lastBillingPeriodNonFatalErrors; // UINT8 (see Appendix B)
    private int lastBillingPeriodFatalErrors; // UINT8 (see Appendix B)
    private int lastBillingPeriodDiagnosticErrors; // UINT8 (see Appendix B)
    private int lastBillingPeriodDiag1Count; // UINT8
    private int lastBillingPeriodDiag2Count; // UINT8
    private int lastBillingPeriodDiag3Count; // UINT8
    private int lastBillingPeriodDiag4Count; // UINT8
    private int lastBillingPeriodDiag5Count; // UINT8
    private int lastBillingPeriodDiag5PhaseACount; // UINT8
    private int lastBillingPeriodDiag5PhaseBCount; // UINT8
    private int lastBillingPeriodDiag5PhaseCCount; // UINT8
    private int lastBillingPeriodPowerOutageCount; // UINT8
    private int lastBillingPeriodTimesProgrammedCount; // UINT8
    private int lastBillingPeriodEarlyPowerFailCount; // UINT16

    // Only 3 <= fw
    private int lastBillingPeriodNonFatalErrors2; // UINT8 (see Appendix B)

    // Only 5 <= fw
    private int lastBillingPeriodDiag6Count; // UINT8

    // All FW versions
    private Date timeDateAtEndOfLastBillingPeriod; // UINT32 (in seconds since 00:00:00 01/01/2000)
    private int seasonInUseAtTheEndOfTheLastBillingPeriod;// UINT8 (see Appendix B)

    /** Creates a new instance of ConstantsDataRead */
    public LastBillingPeriodStateDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LastBillingPeriodStateDataRead:\n");
        strBuff.append("   lastBillingPeriodDemandResetCount="+getLastBillingPeriodDemandResetCount()+"\n");
        strBuff.append("   lastBillingPeriodDiag1Count="+getLastBillingPeriodDiag1Count()+"\n");
        strBuff.append("   lastBillingPeriodDiag2Count="+getLastBillingPeriodDiag2Count()+"\n");
        strBuff.append("   lastBillingPeriodDiag3Count="+getLastBillingPeriodDiag3Count()+"\n");
        strBuff.append("   lastBillingPeriodDiag4Count="+getLastBillingPeriodDiag4Count()+"\n");
        strBuff.append("   lastBillingPeriodDiag5Count="+getLastBillingPeriodDiag5Count()+"\n");
        strBuff.append("   lastBillingPeriodDiag5PhaseACount="+getLastBillingPeriodDiag5PhaseACount()+"\n");
        strBuff.append("   lastBillingPeriodDiag5PhaseBCount="+getLastBillingPeriodDiag5PhaseBCount()+"\n");
        strBuff.append("   lastBillingPeriodDiag5PhaseCCount="+getLastBillingPeriodDiag5PhaseCCount()+"\n");
        strBuff.append("   lastBillingPeriodDiag6Count="+getLastBillingPeriodDiag6Count()+"\n");
        strBuff.append("   lastBillingPeriodDiagnosticErrors="+getLastBillingPeriodDiagnosticErrors()+"\n");
        strBuff.append("   lastBillingPeriodEarlyPowerFailCount="+getLastBillingPeriodEarlyPowerFailCount()+"\n");
        strBuff.append("   lastBillingPeriodFatalErrors="+getLastBillingPeriodFatalErrors()+"\n");
        strBuff.append("   lastBillingPeriodNonFatalErrors="+getLastBillingPeriodNonFatalErrors()+"\n");
        strBuff.append("   lastBillingPeriodNonFatalErrors2="+getLastBillingPeriodNonFatalErrors2()+"\n");
        strBuff.append("   lastBillingPeriodPowerOutageCount="+getLastBillingPeriodPowerOutageCount()+"\n");
        strBuff.append("   lastBillingPeriodTimesProgrammedCount="+getLastBillingPeriodTimesProgrammedCount()+"\n");
        strBuff.append("   seasonInUseAtTheEndOfTheLastBillingPeriod="+getSeasonInUseAtTheEndOfTheLastBillingPeriod()+"\n");
        strBuff.append("   timeDateAtEndOfLastBillingPeriod="+getTimeDateAtEndOfLastBillingPeriod()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        float firmware = getDataReadFactory().getConstantsDataRead().getFirmwareVersionRevision();


        setLastBillingPeriodDemandResetCount(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;
        setLastBillingPeriodNonFatalErrors(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodFatalErrors(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodDiagnosticErrors(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodDiag1Count(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodDiag2Count(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodDiag3Count(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodDiag4Count(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodDiag5Count(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodDiag5PhaseACount(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodDiag5PhaseBCount(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodDiag5PhaseCCount(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodPowerOutageCount(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodTimesProgrammedCount(C12ParseUtils.getInt(data,offset++));
        setLastBillingPeriodEarlyPowerFailCount(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;

        if (firmware >= 3)
            setLastBillingPeriodNonFatalErrors2(C12ParseUtils.getInt(data,offset++));

        if (firmware >= 5)
            setLastBillingPeriodDiag6Count(C12ParseUtils.getInt(data,offset++));

        // All FW versions
        setTimeDateAtEndOfLastBillingPeriod(Utils.parseTimeStamp(C12ParseUtils.getLong(data,offset,4, dataOrder), getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getTimeZone()));
        offset+=4;

        setSeasonInUseAtTheEndOfTheLastBillingPeriod(C12ParseUtils.getInt(data,offset++));


    }

    protected void prepareBuild() throws IOException {

        float firmware = getDataReadFactory().getConstantsDataRead().getFirmwareVersionRevision();

        long[] lids=null;

        if (firmware < 3) {
           lids = new long[]{LogicalIDFactory.findLogicalId("LAST_BP_DEMAND_RESET_COUNT").getId(),
                             LogicalIDFactory.findLogicalId("LAST_BP_ALL_STATE_DATA").getId(),
                             LogicalIDFactory.findLogicalId("LAST_BP_TIME_DATE").getId(),
                             LogicalIDFactory.findLogicalId("LAST_BP_SEASON").getId()};
        }
        if (firmware < 5) {
           lids = new long[]{LogicalIDFactory.findLogicalId("LAST_BP_DEMAND_RESET_COUNT").getId(),
                             LogicalIDFactory.findLogicalId("LAST_BP_ALL_STATE_DATA_2").getId(),
                             LogicalIDFactory.findLogicalId("LAST_BP_TIME_DATE").getId(),
                             LogicalIDFactory.findLogicalId("LAST_BP_SEASON").getId()};
        }
        else {
           lids = new long[]{LogicalIDFactory.findLogicalId("LAST_BP_DEMAND_RESET_COUNT").getId(),
                             LogicalIDFactory.findLogicalId("LAST_BP_ALL_STATE_DATA_3").getId(),
                             LogicalIDFactory.findLogicalId("LAST_BP_TIME_DATE").getId(),
                             LogicalIDFactory.findLogicalId("LAST_BP_SEASON").getId()};
        }


        setDataReadDescriptor(new DataReadDescriptor(0x00, 0x04, lids));

    } // protected void prepareBuild() throws IOException

    public int getLastBillingPeriodDemandResetCount() {
        return lastBillingPeriodDemandResetCount;
    }

    public void setLastBillingPeriodDemandResetCount(int lastBillingPeriodDemandResetCount) {
        this.lastBillingPeriodDemandResetCount = lastBillingPeriodDemandResetCount;
    }

    public int getLastBillingPeriodNonFatalErrors() {
        return lastBillingPeriodNonFatalErrors;
    }

    public void setLastBillingPeriodNonFatalErrors(int lastBillingPeriodNonFatalErrors) {
        this.lastBillingPeriodNonFatalErrors = lastBillingPeriodNonFatalErrors;
    }

    public int getLastBillingPeriodFatalErrors() {
        return lastBillingPeriodFatalErrors;
    }

    public void setLastBillingPeriodFatalErrors(int lastBillingPeriodFatalErrors) {
        this.lastBillingPeriodFatalErrors = lastBillingPeriodFatalErrors;
    }

    public int getLastBillingPeriodDiagnosticErrors() {
        return lastBillingPeriodDiagnosticErrors;
    }

    public void setLastBillingPeriodDiagnosticErrors(int lastBillingPeriodDiagnosticErrors) {
        this.lastBillingPeriodDiagnosticErrors = lastBillingPeriodDiagnosticErrors;
    }

    public int getLastBillingPeriodDiag1Count() {
        return lastBillingPeriodDiag1Count;
    }

    public void setLastBillingPeriodDiag1Count(int lastBillingPeriodDiag1Count) {
        this.lastBillingPeriodDiag1Count = lastBillingPeriodDiag1Count;
    }

    public int getLastBillingPeriodDiag2Count() {
        return lastBillingPeriodDiag2Count;
    }

    public void setLastBillingPeriodDiag2Count(int lastBillingPeriodDiag2Count) {
        this.lastBillingPeriodDiag2Count = lastBillingPeriodDiag2Count;
    }

    public int getLastBillingPeriodDiag3Count() {
        return lastBillingPeriodDiag3Count;
    }

    public void setLastBillingPeriodDiag3Count(int lastBillingPeriodDiag3Count) {
        this.lastBillingPeriodDiag3Count = lastBillingPeriodDiag3Count;
    }

    public int getLastBillingPeriodDiag4Count() {
        return lastBillingPeriodDiag4Count;
    }

    public void setLastBillingPeriodDiag4Count(int lastBillingPeriodDiag4Count) {
        this.lastBillingPeriodDiag4Count = lastBillingPeriodDiag4Count;
    }

    public int getLastBillingPeriodDiag5Count() {
        return lastBillingPeriodDiag5Count;
    }

    public void setLastBillingPeriodDiag5Count(int lastBillingPeriodDiag5Count) {
        this.lastBillingPeriodDiag5Count = lastBillingPeriodDiag5Count;
    }

    public int getLastBillingPeriodDiag5PhaseACount() {
        return lastBillingPeriodDiag5PhaseACount;
    }

    public void setLastBillingPeriodDiag5PhaseACount(int lastBillingPeriodDiag5PhaseACount) {
        this.lastBillingPeriodDiag5PhaseACount = lastBillingPeriodDiag5PhaseACount;
    }

    public int getLastBillingPeriodDiag5PhaseBCount() {
        return lastBillingPeriodDiag5PhaseBCount;
    }

    public void setLastBillingPeriodDiag5PhaseBCount(int lastBillingPeriodDiag5PhaseBCount) {
        this.lastBillingPeriodDiag5PhaseBCount = lastBillingPeriodDiag5PhaseBCount;
    }

    public int getLastBillingPeriodDiag5PhaseCCount() {
        return lastBillingPeriodDiag5PhaseCCount;
    }

    public void setLastBillingPeriodDiag5PhaseCCount(int lastBillingPeriodDiag5PhaseCCount) {
        this.lastBillingPeriodDiag5PhaseCCount = lastBillingPeriodDiag5PhaseCCount;
    }

    public int getLastBillingPeriodPowerOutageCount() {
        return lastBillingPeriodPowerOutageCount;
    }

    public void setLastBillingPeriodPowerOutageCount(int lastBillingPeriodPowerOutageCount) {
        this.lastBillingPeriodPowerOutageCount = lastBillingPeriodPowerOutageCount;
    }

    public int getLastBillingPeriodTimesProgrammedCount() {
        return lastBillingPeriodTimesProgrammedCount;
    }

    public void setLastBillingPeriodTimesProgrammedCount(int lastBillingPeriodTimesProgrammedCount) {
        this.lastBillingPeriodTimesProgrammedCount = lastBillingPeriodTimesProgrammedCount;
    }

    public int getLastBillingPeriodEarlyPowerFailCount() {
        return lastBillingPeriodEarlyPowerFailCount;
    }

    public void setLastBillingPeriodEarlyPowerFailCount(int lastBillingPeriodEarlyPowerFailCount) {
        this.lastBillingPeriodEarlyPowerFailCount = lastBillingPeriodEarlyPowerFailCount;
    }

    public int getLastBillingPeriodNonFatalErrors2() {
        return lastBillingPeriodNonFatalErrors2;
    }

    public void setLastBillingPeriodNonFatalErrors2(int lastBillingPeriodNonFatalErrors2) {
        this.lastBillingPeriodNonFatalErrors2 = lastBillingPeriodNonFatalErrors2;
    }

    public int getLastBillingPeriodDiag6Count() {
        return lastBillingPeriodDiag6Count;
    }

    public void setLastBillingPeriodDiag6Count(int lastBillingPeriodDiag6Count) {
        this.lastBillingPeriodDiag6Count = lastBillingPeriodDiag6Count;
    }

    public Date getTimeDateAtEndOfLastBillingPeriod() {
        return timeDateAtEndOfLastBillingPeriod;
    }

    public void setTimeDateAtEndOfLastBillingPeriod(Date timeDateAtEndOfLastBillingPeriod) {
        this.timeDateAtEndOfLastBillingPeriod = timeDateAtEndOfLastBillingPeriod;
    }

    public int getSeasonInUseAtTheEndOfTheLastBillingPeriod() {
        return seasonInUseAtTheEndOfTheLastBillingPeriod;
    }

    public void setSeasonInUseAtTheEndOfTheLastBillingPeriod(int seasonInUseAtTheEndOfTheLastBillingPeriod) {
        this.seasonInUseAtTheEndOfTheLastBillingPeriod = seasonInUseAtTheEndOfTheLastBillingPeriod;
    }

} // public class ConstantsDataRead extends AbstractDataRead
