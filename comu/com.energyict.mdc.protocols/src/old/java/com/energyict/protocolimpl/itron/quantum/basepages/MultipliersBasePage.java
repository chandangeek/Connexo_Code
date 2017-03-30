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

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class MultipliersBasePage extends AbstractBasePage {

    private BigDecimal demand;
    private BigDecimal voltSquare;
    private BigDecimal ampSquare;
    private BigDecimal energy;


    /** Creates a new instance of RealTimeBasePage */
    public MultipliersBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MultipliersBasePage:\n");
        strBuff.append("   demand="+getDemand()+"\n");
        strBuff.append("   voltSquare="+getVoltSquare()+"\n");
        strBuff.append("   ampSquare="+getAmpSquare()+"\n");
        strBuff.append("   energy="+getEnergy()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(811,831-811);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setDemand(BigDecimal.valueOf(ProtocolUtils.getLong(data,offset,5)));
        offset+=5;
        setVoltSquare(BigDecimal.valueOf(ProtocolUtils.getLong(data,offset,5)));
        offset+=5;
        setAmpSquare(BigDecimal.valueOf(ProtocolUtils.getLong(data,offset,5)));
        offset+=5;
        setEnergy(BigDecimal.valueOf(ProtocolUtils.getLong(data,offset,5)));
        offset+=5;

    }

    public BigDecimal getDemand() {
        return demand;
    }

    public void setDemand(BigDecimal demand) {
        this.demand = demand;
    }

    public BigDecimal getVoltSquare() {
        return voltSquare;
    }

    public void setVoltSquare(BigDecimal voltSquare) {
        this.voltSquare = voltSquare;
    }

    public BigDecimal getAmpSquare() {
        return ampSquare;
    }

    public void setAmpSquare(BigDecimal ampSquare) {
        this.ampSquare = ampSquare;
    }

    public BigDecimal getEnergy() {
        return energy;
    }

    public void setEnergy(BigDecimal energy) {
        this.energy = energy;
    }


} // public class RealTimeBasePage extends AbstractBasePage
