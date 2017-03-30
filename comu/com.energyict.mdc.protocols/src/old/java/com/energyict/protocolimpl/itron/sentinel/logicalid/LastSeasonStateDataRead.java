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
public class LastSeasonStateDataRead extends AbstractDataRead {

    // All FW versions
    private int lastSeasonDemandResetCount; // UINT16
    private int lastSeasonNonFatalErrors; // UINT8 (see Appendix B)
    private int lastSeasonFatalErrors; // UINT8 (see Appendix B)
    private int lastSeasonDiagnosticErrors; // UINT8 (see Appendix B)
    private int lastSeasonDiag1Count; // UINT8
    private int lastSeasonDiag2Count; // UINT8
    private int lastSeasonDiag3Count; // UINT8
    private int lastSeasonDiag4Count; // UINT8
    private int lastSeasonDiag5Count; // UINT8
    private int lastSeasonDiag5PhaseACount; // UINT8
    private int lastSeasonDiag5PhaseBCount; // UINT8
    private int lastSeasonDiag5PhaseCCount; // UINT8
    private int lastSeasonPowerOutageCount; // UINT8
    private int lastSeasonTimesProgrammedCount; // UINT8
    private int lastSeasonEarlyPowerFailCount; // UINT16

    // Only 3 <= fw
    private int lastSeasonNonFatalErrors2; // UINT8 (see Appendix B)

    // Only 5 <= fw
    private int lastSeasonDiag6Count; // UINT8

    // All FW versions
    private Date timeDateAtEndOfLastSeason; // UINT32 (in seconds since 00:00:00 01/01/2000)
    private int seasonInUseAtTheEndOfTheLastSeason;// UINT8 (see Appendix B)

    /** Creates a new instance of ConstantsDataRead */
    public LastSeasonStateDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LastSeasonStateDataRead:\n");
        strBuff.append("   lastSeasonDemandResetCount="+getLastSeasonDemandResetCount()+"\n");
        strBuff.append("   lastSeasonDiag1Count="+getLastSeasonDiag1Count()+"\n");
        strBuff.append("   lastSeasonDiag2Count="+getLastSeasonDiag2Count()+"\n");
        strBuff.append("   lastSeasonDiag3Count="+getLastSeasonDiag3Count()+"\n");
        strBuff.append("   lastSeasonDiag4Count="+getLastSeasonDiag4Count()+"\n");
        strBuff.append("   lastSeasonDiag5Count="+getLastSeasonDiag5Count()+"\n");
        strBuff.append("   lastSeasonDiag5PhaseACount="+getLastSeasonDiag5PhaseACount()+"\n");
        strBuff.append("   lastSeasonDiag5PhaseBCount="+getLastSeasonDiag5PhaseBCount()+"\n");
        strBuff.append("   lastSeasonDiag5PhaseCCount="+getLastSeasonDiag5PhaseCCount()+"\n");
        strBuff.append("   lastSeasonDiag6Count="+getLastSeasonDiag6Count()+"\n");
        strBuff.append("   lastSeasonDiagnosticErrors="+getLastSeasonDiagnosticErrors()+"\n");
        strBuff.append("   lastSeasonEarlyPowerFailCount="+getLastSeasonEarlyPowerFailCount()+"\n");
        strBuff.append("   lastSeasonFatalErrors="+getLastSeasonFatalErrors()+"\n");
        strBuff.append("   lastSeasonNonFatalErrors="+getLastSeasonNonFatalErrors()+"\n");
        strBuff.append("   lastSeasonNonFatalErrors2="+getLastSeasonNonFatalErrors2()+"\n");
        strBuff.append("   lastSeasonPowerOutageCount="+getLastSeasonPowerOutageCount()+"\n");
        strBuff.append("   lastSeasonTimesProgrammedCount="+getLastSeasonTimesProgrammedCount()+"\n");
        strBuff.append("   seasonInUseAtTheEndOfTheLastSeason="+getSeasonInUseAtTheEndOfTheLastSeason()+"\n");
        strBuff.append("   timeDateAtEndOfLastSeason="+getTimeDateAtEndOfLastSeason()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        float firmware = getDataReadFactory().getConstantsDataRead().getFirmwareVersionRevision();


        setLastSeasonDemandResetCount(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;
        setLastSeasonNonFatalErrors(C12ParseUtils.getInt(data,offset++));
        setLastSeasonFatalErrors(C12ParseUtils.getInt(data,offset++));
        setLastSeasonDiagnosticErrors(C12ParseUtils.getInt(data,offset++));
        setLastSeasonDiag1Count(C12ParseUtils.getInt(data,offset++));
        setLastSeasonDiag2Count(C12ParseUtils.getInt(data,offset++));
        setLastSeasonDiag3Count(C12ParseUtils.getInt(data,offset++));
        setLastSeasonDiag4Count(C12ParseUtils.getInt(data,offset++));
        setLastSeasonDiag5Count(C12ParseUtils.getInt(data,offset++));
        setLastSeasonDiag5PhaseACount(C12ParseUtils.getInt(data,offset++));
        setLastSeasonDiag5PhaseBCount(C12ParseUtils.getInt(data,offset++));
        setLastSeasonDiag5PhaseCCount(C12ParseUtils.getInt(data,offset++));
        setLastSeasonPowerOutageCount(C12ParseUtils.getInt(data,offset++));
        setLastSeasonTimesProgrammedCount(C12ParseUtils.getInt(data,offset++));
        setLastSeasonEarlyPowerFailCount(C12ParseUtils.getInt(data,offset, 2, dataOrder));
        offset+=2;

        if (firmware >= 3)
            setLastSeasonNonFatalErrors2(C12ParseUtils.getInt(data,offset++));

        if (firmware >= 5)
            setLastSeasonDiag6Count(C12ParseUtils.getInt(data,offset++));

        // All FW versions
        setTimeDateAtEndOfLastSeason(Utils.parseTimeStamp(C12ParseUtils.getLong(data,offset,4, dataOrder), getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getTimeZone()));
        offset+=4;

        setSeasonInUseAtTheEndOfTheLastSeason(C12ParseUtils.getInt(data,offset++));


    }

    protected void prepareBuild() throws IOException {

        float firmware = getDataReadFactory().getConstantsDataRead().getFirmwareVersionRevision();

        long[] lids=null;

        if (firmware < 3) {
           lids = new long[]{LogicalIDFactory.findLogicalId("LAST_SEAS_DEMAND_RESET_COUNT").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SEAS_ALL_STATE_DATA").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SEAS_TIME_DATE").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SEAS_SEASON").getId()};
        }
        if (firmware < 5) {
           lids = new long[]{LogicalIDFactory.findLogicalId("LAST_SEAS_DEMAND_RESET_COUNT").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SEAS_ALL_STATE_DATA_2").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SEAS_TIME_DATE").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SEAS_SEASON").getId()};
        }
        else {
           lids = new long[]{LogicalIDFactory.findLogicalId("LAST_SEAS_DEMAND_RESET_COUNT").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SEAS_ALL_STATE_DATA_3").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SEAS_TIME_DATE").getId(),
                             LogicalIDFactory.findLogicalId("LAST_SEAS_SEASON").getId()};
        }


        setDataReadDescriptor(new DataReadDescriptor(0x00, 0x04, lids));

    } // protected void prepareBuild() throws IOException

    public int getLastSeasonDemandResetCount() {
        return lastSeasonDemandResetCount;
    }

    public void setLastSeasonDemandResetCount(int lastSeasonDemandResetCount) {
        this.lastSeasonDemandResetCount = lastSeasonDemandResetCount;
    }

    public int getLastSeasonNonFatalErrors() {
        return lastSeasonNonFatalErrors;
    }

    public void setLastSeasonNonFatalErrors(int lastSeasonNonFatalErrors) {
        this.lastSeasonNonFatalErrors = lastSeasonNonFatalErrors;
    }

    public int getLastSeasonFatalErrors() {
        return lastSeasonFatalErrors;
    }

    public void setLastSeasonFatalErrors(int lastSeasonFatalErrors) {
        this.lastSeasonFatalErrors = lastSeasonFatalErrors;
    }

    public int getLastSeasonDiagnosticErrors() {
        return lastSeasonDiagnosticErrors;
    }

    public void setLastSeasonDiagnosticErrors(int lastSeasonDiagnosticErrors) {
        this.lastSeasonDiagnosticErrors = lastSeasonDiagnosticErrors;
    }

    public int getLastSeasonDiag1Count() {
        return lastSeasonDiag1Count;
    }

    public void setLastSeasonDiag1Count(int lastSeasonDiag1Count) {
        this.lastSeasonDiag1Count = lastSeasonDiag1Count;
    }

    public int getLastSeasonDiag2Count() {
        return lastSeasonDiag2Count;
    }

    public void setLastSeasonDiag2Count(int lastSeasonDiag2Count) {
        this.lastSeasonDiag2Count = lastSeasonDiag2Count;
    }

    public int getLastSeasonDiag3Count() {
        return lastSeasonDiag3Count;
    }

    public void setLastSeasonDiag3Count(int lastSeasonDiag3Count) {
        this.lastSeasonDiag3Count = lastSeasonDiag3Count;
    }

    public int getLastSeasonDiag4Count() {
        return lastSeasonDiag4Count;
    }

    public void setLastSeasonDiag4Count(int lastSeasonDiag4Count) {
        this.lastSeasonDiag4Count = lastSeasonDiag4Count;
    }

    public int getLastSeasonDiag5Count() {
        return lastSeasonDiag5Count;
    }

    public void setLastSeasonDiag5Count(int lastSeasonDiag5Count) {
        this.lastSeasonDiag5Count = lastSeasonDiag5Count;
    }

    public int getLastSeasonDiag5PhaseACount() {
        return lastSeasonDiag5PhaseACount;
    }

    public void setLastSeasonDiag5PhaseACount(int lastSeasonDiag5PhaseACount) {
        this.lastSeasonDiag5PhaseACount = lastSeasonDiag5PhaseACount;
    }

    public int getLastSeasonDiag5PhaseBCount() {
        return lastSeasonDiag5PhaseBCount;
    }

    public void setLastSeasonDiag5PhaseBCount(int lastSeasonDiag5PhaseBCount) {
        this.lastSeasonDiag5PhaseBCount = lastSeasonDiag5PhaseBCount;
    }

    public int getLastSeasonDiag5PhaseCCount() {
        return lastSeasonDiag5PhaseCCount;
    }

    public void setLastSeasonDiag5PhaseCCount(int lastSeasonDiag5PhaseCCount) {
        this.lastSeasonDiag5PhaseCCount = lastSeasonDiag5PhaseCCount;
    }

    public int getLastSeasonPowerOutageCount() {
        return lastSeasonPowerOutageCount;
    }

    public void setLastSeasonPowerOutageCount(int lastSeasonPowerOutageCount) {
        this.lastSeasonPowerOutageCount = lastSeasonPowerOutageCount;
    }

    public int getLastSeasonTimesProgrammedCount() {
        return lastSeasonTimesProgrammedCount;
    }

    public void setLastSeasonTimesProgrammedCount(int lastSeasonTimesProgrammedCount) {
        this.lastSeasonTimesProgrammedCount = lastSeasonTimesProgrammedCount;
    }

    public int getLastSeasonEarlyPowerFailCount() {
        return lastSeasonEarlyPowerFailCount;
    }

    public void setLastSeasonEarlyPowerFailCount(int lastSeasonEarlyPowerFailCount) {
        this.lastSeasonEarlyPowerFailCount = lastSeasonEarlyPowerFailCount;
    }

    public int getLastSeasonNonFatalErrors2() {
        return lastSeasonNonFatalErrors2;
    }

    public void setLastSeasonNonFatalErrors2(int lastSeasonNonFatalErrors2) {
        this.lastSeasonNonFatalErrors2 = lastSeasonNonFatalErrors2;
    }

    public int getLastSeasonDiag6Count() {
        return lastSeasonDiag6Count;
    }

    public void setLastSeasonDiag6Count(int lastSeasonDiag6Count) {
        this.lastSeasonDiag6Count = lastSeasonDiag6Count;
    }

    public Date getTimeDateAtEndOfLastSeason() {
        return timeDateAtEndOfLastSeason;
    }

    public void setTimeDateAtEndOfLastSeason(Date timeDateAtEndOfLastSeason) {
        this.timeDateAtEndOfLastSeason = timeDateAtEndOfLastSeason;
    }

    public int getSeasonInUseAtTheEndOfTheLastSeason() {
        return seasonInUseAtTheEndOfTheLastSeason;
    }

    public void setSeasonInUseAtTheEndOfTheLastSeason(int seasonInUseAtTheEndOfTheLastSeason) {
        this.seasonInUseAtTheEndOfTheLastSeason = seasonInUseAtTheEndOfTheLastSeason;
    }

} // public class ConstantsDataRead extends AbstractDataRead
