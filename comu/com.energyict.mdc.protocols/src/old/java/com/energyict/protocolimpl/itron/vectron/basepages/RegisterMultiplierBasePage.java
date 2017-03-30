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

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author Koen
 */
public class RegisterMultiplierBasePage extends AbstractBasePage {

    private BigDecimal multiplier;

    /** Creates a new instance of RealTimeBasePage */
    public RegisterMultiplierBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterMultiplierBasePage:\n");
        strBuff.append("   multiplier="+getMultiplier()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x1d28,4);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setMultiplier(new BigDecimal((double)Float.intBitsToFloat((int)ProtocolUtils.getLong(data,0,4))));

        //getBasePagesFactory().getFulcrum().getTimeZone()
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }


} // public class RealTimeBasePage extends AbstractBasePage
