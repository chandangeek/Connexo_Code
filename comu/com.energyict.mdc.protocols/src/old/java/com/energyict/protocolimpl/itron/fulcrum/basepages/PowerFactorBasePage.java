/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.itron.protocol.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class PowerFactorBasePage extends AbstractBasePage {

    private BigDecimal instantaneousPowerFactor;
    private BigDecimal averagePowerFactor;
    private BigDecimal previousPowerFactor;
    private BigDecimal minimumPowerFactor;
    private Date minimumPowerFactorDate;
    private BigDecimal[] minimumPowerFactorRates;
    private Date[] minimumPowerFactorRateDates;
    private BigDecimal wattHourReadingAtDemandReset;
    private BigDecimal vAHourReadingAtDemandReset;
    private BigDecimal powerFactorMinimumCoincidentRegisterValue;


    /** Creates a new instance of RealTimeBasePage */
    public PowerFactorBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PowerFactorBasePage:\n");
        strBuff.append("   instantaneousPowerFactor="+getInstantaneousPowerFactor()+"\n");
        strBuff.append("   averagePowerFactor="+getAveragePowerFactor()+"\n");
        strBuff.append("   previousPowerFactor="+getPreviousPowerFactor()+"\n");
        strBuff.append("   minimumPowerFactor="+getMinimumPowerFactor()+", minimumPowerFactorDate="+getMinimumPowerFactorDate()+"\n");
        for (int i=0;i<getMinimumPowerFactorRates().length;i++) {
            strBuff.append("       minimumPowerFactorRates["+i+"]="+getMinimumPowerFactorRates()[i]+", minimumPowerFactorRateDates["+i+"]="+getMinimumPowerFactorRateDates()[i]+"\n");
        }
        strBuff.append("   wattHourReadingAtDemandReset="+getWattHourReadingAtDemandReset()+"\n");
        strBuff.append("   VAHourReadingAtDemandReset="+getVAHourReadingAtDemandReset()+"\n");
        strBuff.append("   powerFactorMinimumCoincidentRegisterValue="+getPowerFactorMinimumCoincidentRegisterValue()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x2A87,69);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setInstantaneousPowerFactor(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        setAveragePowerFactor(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        setPreviousPowerFactor(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        setMinimumPowerFactor(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;

        TimeZone tz = ((BasePagesFactory)getBasePagesFactory()).getFulcrum().getTimeZone();
        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);

        setMinimumPowerFactorDate(Utils.buildDate(data,offset,tz));
        offset+=5;

        setMinimumPowerFactorRates(new BigDecimal[RegisterFactory.MAX_NR_OF_RATES]);
        for (int i=0;i<RegisterFactory.MAX_NR_OF_RATES;i++) {
            getMinimumPowerFactorRates()[i] = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
            offset+=4;
        }

        setMinimumPowerFactorRateDates(new Date[RegisterFactory.MAX_NR_OF_RATES]);
        for (int i=0;i<RegisterFactory.MAX_NR_OF_RATES;i++) {
            getMinimumPowerFactorRateDates()[i] = Utils.buildDate(data,offset,tz);
            offset+=5;
        }

        setWattHourReadingAtDemandReset(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        setVAHourReadingAtDemandReset(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        setPowerFactorMinimumCoincidentRegisterValue(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;

    }

    public BigDecimal getInstantaneousPowerFactor() {
        return instantaneousPowerFactor;
    }

    public void setInstantaneousPowerFactor(BigDecimal instantaneousPowerFactor) {
        this.instantaneousPowerFactor = instantaneousPowerFactor;
    }

    public BigDecimal getAveragePowerFactor() {
        return averagePowerFactor;
    }

    public void setAveragePowerFactor(BigDecimal averagePowerFactor) {
        this.averagePowerFactor = averagePowerFactor;
    }

    public BigDecimal getPreviousPowerFactor() {
        return previousPowerFactor;
    }

    public void setPreviousPowerFactor(BigDecimal previousPowerFactor) {
        this.previousPowerFactor = previousPowerFactor;
    }

    public BigDecimal getMinimumPowerFactor() {
        return minimumPowerFactor;
    }

    public void setMinimumPowerFactor(BigDecimal minimumPowerFactor) {
        this.minimumPowerFactor = minimumPowerFactor;
    }

    public Date getMinimumPowerFactorDate() {
        return minimumPowerFactorDate;
    }

    public void setMinimumPowerFactorDate(Date minimumPowerFactorDate) {
        this.minimumPowerFactorDate = minimumPowerFactorDate;
    }

    public BigDecimal[] getMinimumPowerFactorRates() {
        return minimumPowerFactorRates;
    }

    public void setMinimumPowerFactorRates(BigDecimal[] minimumPowerFactorRates) {
        this.minimumPowerFactorRates = minimumPowerFactorRates;
    }

    public Date[] getMinimumPowerFactorRateDates() {
        return minimumPowerFactorRateDates;
    }

    public void setMinimumPowerFactorRateDates(Date[] minimumPowerFactorRateDates) {
        this.minimumPowerFactorRateDates = minimumPowerFactorRateDates;
    }

    public BigDecimal getWattHourReadingAtDemandReset() {
        return wattHourReadingAtDemandReset;
    }

    public void setWattHourReadingAtDemandReset(BigDecimal wattHourReadingAtDemandReset) {
        this.wattHourReadingAtDemandReset = wattHourReadingAtDemandReset;
    }

    public BigDecimal getVAHourReadingAtDemandReset() {
        return vAHourReadingAtDemandReset;
    }

    public void setVAHourReadingAtDemandReset(BigDecimal vAHourReadingAtDemandReset) {
        this.vAHourReadingAtDemandReset = vAHourReadingAtDemandReset;
    }

    public BigDecimal getPowerFactorMinimumCoincidentRegisterValue() {
        return powerFactorMinimumCoincidentRegisterValue;
    }

    public void setPowerFactorMinimumCoincidentRegisterValue(BigDecimal powerFactorMinimumCoincidentRegisterValue) {
        this.powerFactorMinimumCoincidentRegisterValue = powerFactorMinimumCoincidentRegisterValue;
    }


} // public class RealTimeBasePage extends AbstractBasePage
