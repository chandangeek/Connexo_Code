/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataReadingCommandFactory.java
 *
 * Created on 25 oktober 2004, 11:58
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class DataReadingCommandFactory {


    EnermetBase enermet;


    // cached objects
    VersionRead versionRead = null;
    RegisterSet[] registerSets=new RegisterSet[RegisterSetFactory.NR_OF_BILLINGPOINTS];
    BillingPeriodEndCounter billingPeriodEndCounter=null;
    TimeZoneRead timeZoneRead = null;
    RegisterStatusRead registerStatusRead=null;
    EventLogRead eventLogRead=null;
    ConfigInfoRead configInfoRead=null;
    CTVTRead ctvtRatio=null;
    /** Creates a new instance of DataReadingCommandFactory */
    public DataReadingCommandFactory(EnermetBase enermet) {
        this.enermet=enermet;
    }

    /**
     * Getter for property enermet.
     * @return Value of property enermet.
     */
    public com.energyict.protocolimpl.iec1107.enermete70x.EnermetBase getEnermet() {
        return enermet;
    }

    /**
     * Setter for property enermet.
     * @param enermet New value of property enermet.
     */
    public void setEnermet(com.energyict.protocolimpl.iec1107.enermete70x.EnermetBase enermet) {
        this.enermet = enermet;
    }
    /**
     * Getter for property meterExceptionInfo.
     * @return Value of property meterExceptionInfo.
     */
    public MeterExceptionInfo getMeterExceptionInfo() {
        return (MeterExceptionInfo)enermet;
    }

    public Date getDateTimeGmt() throws IOException {
        RealTimeRW rtr = new RealTimeRW(this);
        return rtr.getDate();
    }

    public void setDateTimeGmt(Date date) throws IOException {
        RealTimeRW rtr = new RealTimeRW(this);
        rtr.setDate(date);
    }

    public String getFirmwareVersion() throws IOException {
        if (versionRead == null) {
           versionRead = new VersionRead(this);
        }
        return versionRead.getVersion();
    }


    public String getCTVTRatio() throws IOException {
        if (ctvtRatio == null) {
            ctvtRatio = new CTVTRead(this);
            ctvtRatio.retrieveCTVTRatio();
        }
        return ctvtRatio.getCtvtRatio();
    }

    public String getConfigInfo() throws IOException {
        if (configInfoRead == null) {
            configInfoRead = new ConfigInfoRead(this);
            configInfoRead.retrieveConfigInfoRead();
        }
        return configInfoRead.toString();
    }

    public RegisterSet getCurrentRegisterSet() throws IOException {
        return getRegisterSet(0);
    }

    public RegisterSet getRegisterSet(int billingPoint) throws IOException {
        if (billingPoint >= RegisterSetFactory.NR_OF_BILLINGPOINTS)
            throw new NoSuchRegisterException("No registerset for billingPoint "+billingPoint+" exist! Max 15 billingpoints, F=0..14");
        if (registerSets[billingPoint]==null) {
           RegisterSetFactory rs = new RegisterSetFactory(this);
           registerSets[billingPoint] = rs.getRegisterSet(billingPoint);
        }
        return registerSets[billingPoint];
    }

    public HistorySeriesRead getHistorySeriesRead() {
        HistorySeriesRead hsr = new HistorySeriesRead(this);
        //hsr.getProfileDataBlock(from,nrOfIntervals);
        return hsr;
    }

    public int getBillingPeriodEndCounter(int regId) throws IOException {
        if (billingPeriodEndCounter==null) {
            billingPeriodEndCounter = new BillingPeriodEndCounter(this);
        }
        return billingPeriodEndCounter.getBillingPeriodEndCounter(regId);
    }

    public TimeZone getTimeZoneRead() throws IOException {
        if (timeZoneRead == null)
            timeZoneRead = new TimeZoneRead(this);
        return timeZoneRead.getGMTTimeZone();
    }

    public RegisterStatusRead getRegisterStatusRead() {
        if (registerStatusRead == null)
           registerStatusRead = new RegisterStatusRead(this);
        return registerStatusRead;
    }

    public EventLogRead getEventLog() throws IOException {
        if (eventLogRead == null)
           eventLogRead = new EventLogRead(this);
        return eventLogRead;
    }

    public String getSerialNumber() throws IOException {
        return (new GenericRegisterRead(this)).getRegister("F007");
    }


}