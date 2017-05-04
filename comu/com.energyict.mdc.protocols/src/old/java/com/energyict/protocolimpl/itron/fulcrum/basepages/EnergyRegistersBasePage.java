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

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class EnergyRegistersBasePage extends AbstractBasePage {

    private EnergyRegister wattHour;
    private EnergyRegister varHourLagging;
    private EnergyRegister vAHour;
    private EnergyRegister qHour;
    private EnergyRegister varHourLeadingOrTotal;
    private EnergyRegister voltsSquaredHour;
    private EnergyRegister ampHour;


    /** Creates a new instance of RealTimeBasePage */
    public EnergyRegistersBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EnergyRegistersBasePage:\n");
        strBuff.append("   QHour="+getQHour()+"\n");
        strBuff.append("   VAHour="+getVAHour()+"\n");
        strBuff.append("   ampHour="+getAmpHour()+"\n");
        strBuff.append("   varHourLagging="+getVarHourLagging()+"\n");
        strBuff.append("   varHourLeadingOrTotal="+getVarHourLeadingOrTotal()+"\n");
        strBuff.append("   voltsSquaredHour="+getVoltsSquaredHour()+"\n");
        strBuff.append("   wattHour="+getWattHour()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x2819,28*7);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setWattHour(new EnergyRegister(data,offset)); offset+=EnergyRegister.size();
        setVarHourLagging(new EnergyRegister(data,offset)); offset+=EnergyRegister.size();
        setVAHour(new EnergyRegister(data,offset)); offset+=EnergyRegister.size();
        setQHour(new EnergyRegister(data,offset)); offset+=EnergyRegister.size();
        setVarHourLeadingOrTotal(new EnergyRegister(data,offset)); offset+=EnergyRegister.size();
        setVoltsSquaredHour(new EnergyRegister(data,offset)); offset+=EnergyRegister.size();
        setAmpHour(new EnergyRegister(data,offset)); offset+=EnergyRegister.size();
    }

    public EnergyRegister getWattHour() {
        return wattHour;
    }

    public void setWattHour(EnergyRegister wattHour) {
        this.wattHour = wattHour;
    }

    public EnergyRegister getVarHourLagging() {
        return varHourLagging;
    }

    public void setVarHourLagging(EnergyRegister varHourLagging) {
        this.varHourLagging = varHourLagging;
    }

    public EnergyRegister getVAHour() {
        return vAHour;
    }

    public void setVAHour(EnergyRegister vAHour) {
        this.vAHour = vAHour;
    }

    public EnergyRegister getQHour() {
        return qHour;
    }

    public void setQHour(EnergyRegister qHour) {
        this.qHour = qHour;
    }

    public EnergyRegister getVarHourLeadingOrTotal() {
        return varHourLeadingOrTotal;
    }

    public void setVarHourLeadingOrTotal(EnergyRegister varHourLeadingOrTotal) {
        this.varHourLeadingOrTotal = varHourLeadingOrTotal;
    }

    public EnergyRegister getVoltsSquaredHour() {
        return voltsSquaredHour;
    }

    public void setVoltsSquaredHour(EnergyRegister voltsSquaredHour) {
        this.voltsSquaredHour = voltsSquaredHour;
    }

    public EnergyRegister getAmpHour() {
        return ampHour;
    }

    public void setAmpHour(EnergyRegister ampHour) {
        this.ampHour = ampHour;
    }


} // public class RealTimeBasePage extends AbstractBasePage
