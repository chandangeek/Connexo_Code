/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CoincidentDemandSetupTableBasePage.java
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

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CoincidentDemandSetupTableBasePage extends AbstractBasePage {

    private int addressForkWPeak;
    private int rateForkWPeak; // 0=total, 1..4 = rate A..D
    private int addressForkvarPeak;
    private int rateForkvarPeak; // 0=total, 1..4 = rate A..D
    private int addressForkVAPeak;
    private int rateForkVAPeak; // 0=total, 1..4 = rate A..D
    private int addressForMinPFPeak;
    private int rateForMinPFPeak; // 0=total, 1..4 = rate A..D


    /** Creates a new instance of CoincidentDemandSetupTableBasePage */
    public CoincidentDemandSetupTableBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CoincidentDemandSetupTableBasePage:\n");
        strBuff.append("   addressForkWPeak=0x"+Integer.toHexString(getAddressForkWPeak())+"\n");
        strBuff.append("   rateForkWPeak="+getRateForkWPeak()+"\n");
        strBuff.append("   addressForMinPFPeak=0x"+Integer.toHexString(getAddressForMinPFPeak())+"\n");
        strBuff.append("   rateForMinPFPeak="+getRateForMinPFPeak()+"\n");
        strBuff.append("   addressForkVAPeak=0x"+Integer.toHexString(getAddressForkVAPeak())+"\n");
        strBuff.append("   rateForkVAPeak="+getRateForkVAPeak()+"\n");
        strBuff.append("   addressForkvarPeak=0x"+Integer.toHexString(getAddressForkvarPeak())+"\n");
        strBuff.append("   rateForkvarPeak="+getRateForkvarPeak()+"\n");
        return strBuff.toString();
    }


    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x259E,12);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setAddressForkWPeak(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setRateForkWPeak(ProtocolUtils.getInt(data,offset,1));
        offset++;
        setAddressForkvarPeak(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setRateForkvarPeak(ProtocolUtils.getInt(data,offset,1));
        offset++;
        setAddressForkVAPeak(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setRateForkVAPeak(ProtocolUtils.getInt(data,offset,1));
        offset++;
        setAddressForMinPFPeak(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setRateForMinPFPeak(ProtocolUtils.getInt(data,offset,1));
        offset++;
    }

    public int getAddressForkWPeak() {
        return addressForkWPeak;
    }

    public void setAddressForkWPeak(int addressForkWPeak) {
        this.addressForkWPeak = addressForkWPeak;
    }

    public int getRateForkWPeak() {
        return rateForkWPeak;
    }

    public void setRateForkWPeak(int rateForkWPeak) {
        this.rateForkWPeak = rateForkWPeak;
    }

    public int getAddressForkvarPeak() {
        return addressForkvarPeak;
    }

    public void setAddressForkvarPeak(int addressForkvarPeak) {
        this.addressForkvarPeak = addressForkvarPeak;
    }

    public int getRateForkvarPeak() {
        return rateForkvarPeak;
    }

    public void setRateForkvarPeak(int rateForkvarPeak) {
        this.rateForkvarPeak = rateForkvarPeak;
    }

    public int getAddressForkVAPeak() {
        return addressForkVAPeak;
    }

    public void setAddressForkVAPeak(int addressForkVAPeak) {
        this.addressForkVAPeak = addressForkVAPeak;
    }

    public int getRateForkVAPeak() {
        return rateForkVAPeak;
    }

    public void setRateForkVAPeak(int rateForkVAPeak) {
        this.rateForkVAPeak = rateForkVAPeak;
    }

    public int getAddressForMinPFPeak() {
        return addressForMinPFPeak;
    }

    public void setAddressForMinPFPeak(int addressForMinPFPeak) {
        this.addressForMinPFPeak = addressForMinPFPeak;
    }

    public int getRateForMinPFPeak() {
        return rateForMinPFPeak;
    }

    public void setRateForMinPFPeak(int rateForMinPFPeak) {
        this.rateForMinPFPeak = rateForMinPFPeak;
    }



} // public class RealTimeBasePage extends AbstractBasePage
