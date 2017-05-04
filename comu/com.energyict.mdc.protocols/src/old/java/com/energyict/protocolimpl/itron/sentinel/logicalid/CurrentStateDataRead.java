/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
public class CurrentStateDataRead extends AbstractDataRead {


    private int currentDemandResetCount;  // UINT16
    private int currentNonFatalErrors; // UINT8 (see Appendix B)
    private int currentFatalErrors; // UINT8 (see Appendix B)
    private int currentDiagnosticErrors; // UINT8 (see Appendix B)
    private int currentDiag1Count; // UINT8
    private int currentDiag2Count; // UINT8
    private int currentDiag3Count; // UINT8
    private int currentDiag4Count; // UINT8
    private int currentDiag5Count; // UINT8
    private int currentDiag5PhaseACount; // UINT8
    private int currentDiag5PhaseBCount; // UINT8
    private int currentDiag5PhaseCCount; // UINT8
    private int currentPowerOutageCount; // UINT8
    private int currentTimesProgrammedCount; // UINT8
    private int currentEarlyPowerFailCount; // UINT16
    private int currentNonFatalErrors2; // UINT8 (see Appendix B)
    private int currentDiag6Count; // UINT8 only for FW versions 5 and higher otherwise non existing
    private Date currentTimeDate; // UINT32 (in seconds since 00:00:00 01/01/2000)
    private int currentSeason; // UINT8 (see Appendix B)

    /** Creates a new instance of ConstantsDataRead */
    public CurrentStateDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CurrentStateDataRead:\n");
        strBuff.append("   currentDemandResetCount="+getCurrentDemandResetCount()+"\n");
        strBuff.append("   currentDiag1Count="+getCurrentDiag1Count()+"\n");
        strBuff.append("   currentDiag2Count="+getCurrentDiag2Count()+"\n");
        strBuff.append("   currentDiag3Count="+getCurrentDiag3Count()+"\n");
        strBuff.append("   currentDiag4Count="+getCurrentDiag4Count()+"\n");
        strBuff.append("   currentDiag5Count="+getCurrentDiag5Count()+"\n");
        strBuff.append("   currentDiag5PhaseACount="+getCurrentDiag5PhaseACount()+"\n");
        strBuff.append("   currentDiag5PhaseBCount="+getCurrentDiag5PhaseBCount()+"\n");
        strBuff.append("   currentDiag5PhaseCCount="+getCurrentDiag5PhaseCCount()+"\n");
        strBuff.append("   currentDiag6Count="+getCurrentDiag6Count()+"\n");
        strBuff.append("   currentDiagnosticErrors="+getCurrentDiagnosticErrors()+"\n");
        strBuff.append("   currentEarlyPowerFailCount="+getCurrentEarlyPowerFailCount()+"\n");
        strBuff.append("   currentFatalErrors="+getCurrentFatalErrors()+"\n");
        strBuff.append("   currentNonFatalErrors="+getCurrentNonFatalErrors()+"\n");
        strBuff.append("   currentNonFatalErrors2="+getCurrentNonFatalErrors2()+"\n");
        strBuff.append("   currentPowerOutageCount="+getCurrentPowerOutageCount()+"\n");
        strBuff.append("   currentSeason="+getCurrentSeason()+"\n");
        strBuff.append("   currentTimeDate="+getCurrentTimeDate()+"\n");
        strBuff.append("   currentTimesProgrammedCount="+getCurrentTimesProgrammedCount()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setCurrentDemandResetCount(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setCurrentNonFatalErrors(C12ParseUtils.getInt(data,offset++));
        setCurrentFatalErrors(C12ParseUtils.getInt(data,offset++));
        setCurrentDiagnosticErrors(C12ParseUtils.getInt(data,offset++));
        setCurrentDiag1Count(C12ParseUtils.getInt(data,offset++));
        setCurrentDiag2Count(C12ParseUtils.getInt(data,offset++));
        setCurrentDiag3Count(C12ParseUtils.getInt(data,offset++));
        setCurrentDiag4Count(C12ParseUtils.getInt(data,offset++));
        setCurrentDiag5Count(C12ParseUtils.getInt(data,offset++));
        setCurrentDiag5PhaseACount(C12ParseUtils.getInt(data,offset++));
        setCurrentDiag5PhaseBCount(C12ParseUtils.getInt(data,offset++));
        setCurrentDiag5PhaseCCount(C12ParseUtils.getInt(data,offset++));
        setCurrentPowerOutageCount(C12ParseUtils.getInt(data,offset++));
        setCurrentTimesProgrammedCount(C12ParseUtils.getInt(data,offset++));
        setCurrentEarlyPowerFailCount(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setCurrentNonFatalErrors2(C12ParseUtils.getInt(data,offset++));

        if (getDataReadFactory().getConstantsDataRead().getFirmwareVersionRevision() >= 5) {
            setCurrentDiag6Count(C12ParseUtils.getInt(data,offset++));
        }

        setCurrentTimeDate(Utils.parseTimeStamp(C12ParseUtils.getLong(data,offset,4, dataOrder), getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getTimeZone()));
        offset+=4;

        setCurrentSeason(C12ParseUtils.getInt(data,offset++));

    }

    protected void prepareBuild() throws IOException {

        long[] lids=null;

        if (getDataReadFactory().getConstantsDataRead().getFirmwareVersionRevision() >= 5) {
            lids = new long[]{LogicalIDFactory.findLogicalId("DEMAND_RESET_COUNT").getId(),
                                     LogicalIDFactory.findLogicalId("ALL_STATE_DATA_3").getId(),
                                     LogicalIDFactory.findLogicalId("CURRENT_TIME_DATE").getId(),
                                     LogicalIDFactory.findLogicalId("CURRENT_SEASON").getId()};
        }
        else {
            lids = new long[]{LogicalIDFactory.findLogicalId("DEMAND_RESET_COUNT").getId(),
                                     LogicalIDFactory.findLogicalId("ALL_STATE_DATA").getId(),
                                     LogicalIDFactory.findLogicalId("CURRENT_TIME_DATE").getId(),
                                     LogicalIDFactory.findLogicalId("CURRENT_SEASON").getId()};
        }

        setDataReadDescriptor(new DataReadDescriptor(0x00, 0x04, lids));

    } // protected void prepareBuild() throws IOException

    public int getCurrentDemandResetCount() {
        return currentDemandResetCount;
    }

    public void setCurrentDemandResetCount(int currentDemandResetCount) {
        this.currentDemandResetCount = currentDemandResetCount;
    }

    public int getCurrentNonFatalErrors() {
        return currentNonFatalErrors;
    }

    public void setCurrentNonFatalErrors(int currentNonFatalErrors) {
        this.currentNonFatalErrors = currentNonFatalErrors;
    }

    public int getCurrentFatalErrors() {
        return currentFatalErrors;
    }

    public void setCurrentFatalErrors(int currentFatalErrors) {
        this.currentFatalErrors = currentFatalErrors;
    }

    public int getCurrentDiagnosticErrors() {
        return currentDiagnosticErrors;
    }

    public void setCurrentDiagnosticErrors(int currentDiagnosticErrors) {
        this.currentDiagnosticErrors = currentDiagnosticErrors;
    }

    public int getCurrentDiag1Count() {
        return currentDiag1Count;
    }

    public void setCurrentDiag1Count(int currentDiag1Count) {
        this.currentDiag1Count = currentDiag1Count;
    }

    public int getCurrentDiag2Count() {
        return currentDiag2Count;
    }

    public void setCurrentDiag2Count(int currentDiag2Count) {
        this.currentDiag2Count = currentDiag2Count;
    }

    public int getCurrentDiag3Count() {
        return currentDiag3Count;
    }

    public void setCurrentDiag3Count(int currentDiag3Count) {
        this.currentDiag3Count = currentDiag3Count;
    }

    public int getCurrentDiag4Count() {
        return currentDiag4Count;
    }

    public void setCurrentDiag4Count(int currentDiag4Count) {
        this.currentDiag4Count = currentDiag4Count;
    }

    public int getCurrentDiag5Count() {
        return currentDiag5Count;
    }

    public void setCurrentDiag5Count(int currentDiag5Count) {
        this.currentDiag5Count = currentDiag5Count;
    }

    public int getCurrentDiag5PhaseACount() {
        return currentDiag5PhaseACount;
    }

    public void setCurrentDiag5PhaseACount(int currentDiag5PhaseACount) {
        this.currentDiag5PhaseACount = currentDiag5PhaseACount;
    }

    public int getCurrentDiag5PhaseBCount() {
        return currentDiag5PhaseBCount;
    }

    public void setCurrentDiag5PhaseBCount(int currentDiag5PhaseBCount) {
        this.currentDiag5PhaseBCount = currentDiag5PhaseBCount;
    }

    public int getCurrentDiag5PhaseCCount() {
        return currentDiag5PhaseCCount;
    }

    public void setCurrentDiag5PhaseCCount(int currentDiag5PhaseCCount) {
        this.currentDiag5PhaseCCount = currentDiag5PhaseCCount;
    }

    public int getCurrentPowerOutageCount() {
        return currentPowerOutageCount;
    }

    public void setCurrentPowerOutageCount(int currentPowerOutageCount) {
        this.currentPowerOutageCount = currentPowerOutageCount;
    }

    public int getCurrentTimesProgrammedCount() {
        return currentTimesProgrammedCount;
    }

    public void setCurrentTimesProgrammedCount(int currentTimesProgrammedCount) {
        this.currentTimesProgrammedCount = currentTimesProgrammedCount;
    }

    public int getCurrentEarlyPowerFailCount() {
        return currentEarlyPowerFailCount;
    }

    public void setCurrentEarlyPowerFailCount(int currentEarlyPowerFailCount) {
        this.currentEarlyPowerFailCount = currentEarlyPowerFailCount;
    }

    public int getCurrentNonFatalErrors2() {
        return currentNonFatalErrors2;
    }

    public void setCurrentNonFatalErrors2(int currentNonFatalErrors2) {
        this.currentNonFatalErrors2 = currentNonFatalErrors2;
    }

    public int getCurrentDiag6Count() {
        return currentDiag6Count;
    }

    public void setCurrentDiag6Count(int currentDiag6Count) {
        this.currentDiag6Count = currentDiag6Count;
    }

    public Date getCurrentTimeDate() {
        return currentTimeDate;
    }

    public void setCurrentTimeDate(Date currentTimeDate) {
        this.currentTimeDate = currentTimeDate;
    }

    public int getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(int currentSeason) {
        this.currentSeason = currentSeason;
    }

} // public class ConstantsDataRead extends AbstractDataRead
