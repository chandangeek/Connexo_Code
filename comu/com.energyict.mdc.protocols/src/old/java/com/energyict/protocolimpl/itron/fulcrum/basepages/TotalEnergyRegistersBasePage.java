/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TotalEnergyRegistersBasePage.java
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
public class TotalEnergyRegistersBasePage extends AbstractBasePage {

    private TotalEnergyRegister wattHour;
    private TotalEnergyRegister varHourLagging;
    private TotalEnergyRegister vAHour;
    private TotalEnergyRegister qHour;
    private TotalEnergyRegister varHourLeadingOrTotal;
    private TotalEnergyRegister voltsSquaredHour;
    private TotalEnergyRegister ampHour;

    /** Creates a new instance of TotalEnergyRegistersBasePage */
    public TotalEnergyRegistersBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TotalEnergyRegistersBasePage:\n");
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
        return new BasePageDescriptor(0x49CB,0x4A57-0x49CB);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setWattHour(new TotalEnergyRegister(data,offset));
        offset+=TotalEnergyRegister.size();
        setVarHourLagging(new TotalEnergyRegister(data,offset));
        offset+=TotalEnergyRegister.size();
        setVAHour(new TotalEnergyRegister(data,offset));
        offset+=TotalEnergyRegister.size();
        setQHour(new TotalEnergyRegister(data,offset));
        offset+=TotalEnergyRegister.size();
        setVarHourLeadingOrTotal(new TotalEnergyRegister(data,offset));
        offset+=TotalEnergyRegister.size();
        setVoltsSquaredHour(new TotalEnergyRegister(data,offset));
        offset+=TotalEnergyRegister.size();
        setAmpHour(new TotalEnergyRegister(data,offset));
        offset+=TotalEnergyRegister.size();
    }

    public TotalEnergyRegister getWattHour() {
        return wattHour;
    }

    public void setWattHour(TotalEnergyRegister wattHour) {
        this.wattHour = wattHour;
    }

    public TotalEnergyRegister getVarHourLagging() {
        return varHourLagging;
    }

    public void setVarHourLagging(TotalEnergyRegister varHourLagging) {
        this.varHourLagging = varHourLagging;
    }

    public TotalEnergyRegister getVAHour() {
        return vAHour;
    }

    public void setVAHour(TotalEnergyRegister vAHour) {
        this.vAHour = vAHour;
    }

    public TotalEnergyRegister getQHour() {
        return qHour;
    }

    public void setQHour(TotalEnergyRegister qHour) {
        this.qHour = qHour;
    }

    public TotalEnergyRegister getVarHourLeadingOrTotal() {
        return varHourLeadingOrTotal;
    }

    public void setVarHourLeadingOrTotal(TotalEnergyRegister varHourLeadingOrTotal) {
        this.varHourLeadingOrTotal = varHourLeadingOrTotal;
    }

    public TotalEnergyRegister getVoltsSquaredHour() {
        return voltsSquaredHour;
    }

    public void setVoltsSquaredHour(TotalEnergyRegister voltsSquaredHour) {
        this.voltsSquaredHour = voltsSquaredHour;
    }

    public TotalEnergyRegister getAmpHour() {
        return ampHour;
    }

    public void setAmpHour(TotalEnergyRegister ampHour) {
        this.ampHour = ampHour;
    }

} // public class RealTimeBasePage extends AbstractBasePage
