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
public class LastSelfReadStateDataRead extends AbstractDataRead {

    // All FW versions
    private int lastSelfReadDemandResetCount; // UINT16
    private int lastSelfReadNonFatalErrors; // UINT8 (see Appendix B)
    private int lastSelfReadFatalErrors; // UINT8 (see Appendix B)
    private int lastSelfReadDiagnosticErrors; // UINT8 (see Appendix B)
    private int lastSelfReadDiag1Count; // UINT8
    private int lastSelfReadDiag2Count; // UINT8
    private int lastSelfReadDiag3Count; // UINT8
    private int lastSelfReadDiag4Count; // UINT8
    private int lastSelfReadDiag5Count; // UINT8
    private int lastSelfReadDiag5PhaseACount; // UINT8
    private int lastSelfReadDiag5PhaseBCount; // UINT8
    private int lastSelfReadDiag5PhaseCCount; // UINT8
    private int lastSelfReadPowerOutageCount; // UINT8
    private int lastSelfReadTimesProgrammedCount; // UINT8
    private int lastSelfReadEarlyPowerFailCount; // UINT16

    // Only 3 <= fw
    private int lastSelfReadNonFatalErrors2; // UINT8 (see Appendix B)

    // Only 5 <= fw
    private int lastSelfReadDiag6Count; // UINT8

    // All FW versions
    private Date timeDateAtEndOfLastSelfRead; // UINT32 (in seconds since 00:00:00 01/01/2000)
    private int seasonInUseAtTheEndOfTheLastSelfRead;// UINT8 (see Appendix B)

    /** Creates a new instance of ConstantsDataRead */
    public LastSelfReadStateDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LastSelfReadStateDataRead:\n");
        strBuff.append("   lastSelfReadDemandResetCount="+getLastSelfReadDemandResetCount()+"\n");
        strBuff.append("   lastSelfReadDiag1Count="+getLastSelfReadDiag1Count()+"\n");
        strBuff.append("   lastSelfReadDiag2Count="+getLastSelfReadDiag2Count()+"\n");
        strBuff.append("   lastSelfReadDiag3Count="+getLastSelfReadDiag3Count()+"\n");
        strBuff.append("   lastSelfReadDiag4Count="+getLastSelfReadDiag4Count()+"\n");
        strBuff.append("   lastSelfReadDiag5Count="+getLastSelfReadDiag5Count()+"\n");
        strBuff.append("   lastSelfReadDiag5PhaseACount="+getLastSelfReadDiag5PhaseACount()+"\n");
        strBuff.append("   lastSelfReadDiag5PhaseBCount="+getLastSelfReadDiag5PhaseBCount()+"\n");
        strBuff.append("   lastSelfReadDiag5PhaseCCount="+getLastSelfReadDiag5PhaseCCount()+"\n");
        strBuff.append("   lastSelfReadDiag6Count="+getLastSelfReadDiag6Count()+"\n");
        strBuff.append("   lastSelfReadDiagnosticErrors="+getLastSelfReadDiagnosticErrors()+"\n");
        strBuff.append("   lastSelfReadEarlyPowerFailCount="+getLastSelfReadEarlyPowerFailCount()+"\n");
        strBuff.append("   lastSelfReadFatalErrors="+getLastSelfReadFatalErrors()+"\n");
        strBuff.append("   lastSelfReadNonFatalErrors="+getLastSelfReadNonFatalErrors()+"\n");
        strBuff.append("   lastSelfReadNonFatalErrors2="+getLastSelfReadNonFatalErrors2()+"\n");
        strBuff.append("   lastSelfReadPowerOutageCount="+getLastSelfReadPowerOutageCount()+"\n");
        strBuff.append("   lastSelfReadTimesProgrammedCount="+getLastSelfReadTimesProgrammedCount()+"\n");
        strBuff.append("   seasonInUseAtTheEndOfTheLastSelfRead="+getSelfReadInUseAtTheEndOfTheLastSelfRead()+"\n");
        strBuff.append("   timeDateAtEndOfLastSelfRead="+getTimeDateAtEndOfLastSelfRead()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        float firmware = getDataReadFactory().getConstantsDataRead().getFirmwareVersionRevision();


        setLastSelfReadDemandResetCount(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;
        setLastSelfReadNonFatalErrors(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadFatalErrors(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadDiagnosticErrors(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadDiag1Count(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadDiag2Count(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadDiag3Count(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadDiag4Count(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadDiag5Count(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadDiag5PhaseACount(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadDiag5PhaseBCount(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadDiag5PhaseCCount(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadPowerOutageCount(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadTimesProgrammedCount(C12ParseUtils.getInt(data,offset++));
        setLastSelfReadEarlyPowerFailCount(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;

        if (firmware >= 3)
            setLastSelfReadNonFatalErrors2(C12ParseUtils.getInt(data,offset++));

        if (firmware >= 5)
            setLastSelfReadDiag6Count(C12ParseUtils.getInt(data,offset++));

        // All FW versions
        setTimeDateAtEndOfLastSelfRead(Utils.parseTimeStamp(C12ParseUtils.getLong(data,offset,4, dataOrder), getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getTimeZone()));
        offset+=4;

        setSelfReadInUseAtTheEndOfTheLastSelfRead(C12ParseUtils.getInt(data,offset++));


    }

    protected void prepareBuild() throws IOException {

        float firmware = getDataReadFactory().getConstantsDataRead().getFirmwareVersionRevision();

        long[] lids=null;

        if (firmware < 3) {
           lids = new long[]{LogicalIDFactory.findLogicalId("LAST_SR_DEMAND_RESET_COUNT").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SR_ALL_STATE_DATA").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SR_TIME_DATE").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SR_SEASON").getId()};
        }
        if (firmware < 5) {
           lids = new long[]{LogicalIDFactory.findLogicalId("LAST_SR_DEMAND_RESET_COUNT").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SR_ALL_STATE_DATA_2").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SR_TIME_DATE").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SR_SEASON").getId()};
        }
        else {
           lids = new long[]{LogicalIDFactory.findLogicalId("LAST_SR_DEMAND_RESET_COUNT").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SR_ALL_STATE_DATA_3").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SR_TIME_DATE").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SR_SEASON").getId()};
        }


        setDataReadDescriptor(new DataReadDescriptor(0x00, 0x04, lids));

    } // protected void prepareBuild() throws IOException

    public int getLastSelfReadDemandResetCount() {
        return lastSelfReadDemandResetCount;
    }

    public void setLastSelfReadDemandResetCount(int lastSelfReadDemandResetCount) {
        this.lastSelfReadDemandResetCount = lastSelfReadDemandResetCount;
    }

    public int getLastSelfReadNonFatalErrors() {
        return lastSelfReadNonFatalErrors;
    }

    public void setLastSelfReadNonFatalErrors(int lastSelfReadNonFatalErrors) {
        this.lastSelfReadNonFatalErrors = lastSelfReadNonFatalErrors;
    }

    public int getLastSelfReadFatalErrors() {
        return lastSelfReadFatalErrors;
    }

    public void setLastSelfReadFatalErrors(int lastSelfReadFatalErrors) {
        this.lastSelfReadFatalErrors = lastSelfReadFatalErrors;
    }

    public int getLastSelfReadDiagnosticErrors() {
        return lastSelfReadDiagnosticErrors;
    }

    public void setLastSelfReadDiagnosticErrors(int lastSelfReadDiagnosticErrors) {
        this.lastSelfReadDiagnosticErrors = lastSelfReadDiagnosticErrors;
    }

    public int getLastSelfReadDiag1Count() {
        return lastSelfReadDiag1Count;
    }

    public void setLastSelfReadDiag1Count(int lastSelfReadDiag1Count) {
        this.lastSelfReadDiag1Count = lastSelfReadDiag1Count;
    }

    public int getLastSelfReadDiag2Count() {
        return lastSelfReadDiag2Count;
    }

    public void setLastSelfReadDiag2Count(int lastSelfReadDiag2Count) {
        this.lastSelfReadDiag2Count = lastSelfReadDiag2Count;
    }

    public int getLastSelfReadDiag3Count() {
        return lastSelfReadDiag3Count;
    }

    public void setLastSelfReadDiag3Count(int lastSelfReadDiag3Count) {
        this.lastSelfReadDiag3Count = lastSelfReadDiag3Count;
    }

    public int getLastSelfReadDiag4Count() {
        return lastSelfReadDiag4Count;
    }

    public void setLastSelfReadDiag4Count(int lastSelfReadDiag4Count) {
        this.lastSelfReadDiag4Count = lastSelfReadDiag4Count;
    }

    public int getLastSelfReadDiag5Count() {
        return lastSelfReadDiag5Count;
    }

    public void setLastSelfReadDiag5Count(int lastSelfReadDiag5Count) {
        this.lastSelfReadDiag5Count = lastSelfReadDiag5Count;
    }

    public int getLastSelfReadDiag5PhaseACount() {
        return lastSelfReadDiag5PhaseACount;
    }

    public void setLastSelfReadDiag5PhaseACount(int lastSelfReadDiag5PhaseACount) {
        this.lastSelfReadDiag5PhaseACount = lastSelfReadDiag5PhaseACount;
    }

    public int getLastSelfReadDiag5PhaseBCount() {
        return lastSelfReadDiag5PhaseBCount;
    }

    public void setLastSelfReadDiag5PhaseBCount(int lastSelfReadDiag5PhaseBCount) {
        this.lastSelfReadDiag5PhaseBCount = lastSelfReadDiag5PhaseBCount;
    }

    public int getLastSelfReadDiag5PhaseCCount() {
        return lastSelfReadDiag5PhaseCCount;
    }

    public void setLastSelfReadDiag5PhaseCCount(int lastSelfReadDiag5PhaseCCount) {
        this.lastSelfReadDiag5PhaseCCount = lastSelfReadDiag5PhaseCCount;
    }

    public int getLastSelfReadPowerOutageCount() {
        return lastSelfReadPowerOutageCount;
    }

    public void setLastSelfReadPowerOutageCount(int lastSelfReadPowerOutageCount) {
        this.lastSelfReadPowerOutageCount = lastSelfReadPowerOutageCount;
    }

    public int getLastSelfReadTimesProgrammedCount() {
        return lastSelfReadTimesProgrammedCount;
    }

    public void setLastSelfReadTimesProgrammedCount(int lastSelfReadTimesProgrammedCount) {
        this.lastSelfReadTimesProgrammedCount = lastSelfReadTimesProgrammedCount;
    }

    public int getLastSelfReadEarlyPowerFailCount() {
        return lastSelfReadEarlyPowerFailCount;
    }

    public void setLastSelfReadEarlyPowerFailCount(int lastSelfReadEarlyPowerFailCount) {
        this.lastSelfReadEarlyPowerFailCount = lastSelfReadEarlyPowerFailCount;
    }

    public int getLastSelfReadNonFatalErrors2() {
        return lastSelfReadNonFatalErrors2;
    }

    public void setLastSelfReadNonFatalErrors2(int lastSelfReadNonFatalErrors2) {
        this.lastSelfReadNonFatalErrors2 = lastSelfReadNonFatalErrors2;
    }

    public int getLastSelfReadDiag6Count() {
        return lastSelfReadDiag6Count;
    }

    public void setLastSelfReadDiag6Count(int lastSelfReadDiag6Count) {
        this.lastSelfReadDiag6Count = lastSelfReadDiag6Count;
    }

    public Date getTimeDateAtEndOfLastSelfRead() {
        return timeDateAtEndOfLastSelfRead;
    }

    public void setTimeDateAtEndOfLastSelfRead(Date timeDateAtEndOfLastSelfRead) {
        this.timeDateAtEndOfLastSelfRead = timeDateAtEndOfLastSelfRead;
    }

    public int getSelfReadInUseAtTheEndOfTheLastSelfRead() {
        return seasonInUseAtTheEndOfTheLastSelfRead;
    }

    public void setSelfReadInUseAtTheEndOfTheLastSelfRead(int seasonInUseAtTheEndOfTheLastSelfRead) {
        this.seasonInUseAtTheEndOfTheLastSelfRead = seasonInUseAtTheEndOfTheLastSelfRead;
    }

} // public class ConstantsDataRead extends AbstractDataRead
