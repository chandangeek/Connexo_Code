/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * FirmwareOptionsBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class FirmwareOptionsBasePage extends AbstractBasePage {

    private int options;

    /** Creates a new instance of FirmwareOptionsBasePage */
    public FirmwareOptionsBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public boolean isDemand() {
        return (getOptions()&0x01)==0x01;
    }

    public boolean isTimeOfUse() {
        return (getOptions()&0x02)==0x02;
    }

    public boolean isMassMemory() {
        return (getOptions()&0x04)==0x04;
    }

    public boolean isSeasonChangeRegisters() {
        return (getOptions()&0x10)==0x10;
    }

    public boolean isReactiveRegisters() {
        return (getOptions()&0x20)==0x20;
    }

    public boolean isLowPowerLowBattery() {
        return (getOptions()&0x40)==0x40;
    }


    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("FirmwareOptionsBasePage:\n");
        strBuff.append("   options=0x"+Integer.toHexString(getOptions())+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x223C,0x1);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setOptions(ProtocolUtils.getInt(data,0,1));
    }

    public int getOptions() {
        return options;
    }

    public void setOptions(int options) {
        this.options = options;
    }


} // public class FirmwareOptionsBasePage extends AbstractBasePage
