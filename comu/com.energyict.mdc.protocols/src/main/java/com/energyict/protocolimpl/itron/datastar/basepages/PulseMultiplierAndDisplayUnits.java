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

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class PulseMultiplierAndDisplayUnits extends AbstractBasePage {

    private BigDecimal[] pulseMultipliers = new BigDecimal[4];
    private Unit[] displayUnits = new Unit[4];


    /** Creates a new instance of RealTimeBasePage */
    public PulseMultiplierAndDisplayUnits(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PulseMultiplierAndDisplayUnits:\n");
        for (int i=0;i<getDisplayUnits().length;i++) {
            strBuff.append("       displayUnits["+i+"]="+getDisplayUnits()[i]+"\n");
        }
        for (int i=0;i<getPulseMultipliers().length;i++) {
            strBuff.append("       pulseMultipliers["+i+"]="+getPulseMultipliers()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x56eb,16);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        for (int i=0;i<getPulseMultipliers().length;i++) {
            getPulseMultipliers()[i] = BigDecimal.valueOf(ProtocolUtils.getLong(data,offset, 3));
            offset+=3;
        }

        for (int i=0;i<getDisplayUnits().length;i++) {
            int temp = ((int)data[offset++] & 0xff);
            if (temp == 1) getDisplayUnits()[i] = Unit.get("kW");
            if (temp == 2) getDisplayUnits()[i] = Unit.get("kvar");
            if (temp == 3) getDisplayUnits()[i] = Unit.get("kvar");
        }

    }

    public BigDecimal[] getPulseMultipliers() {
        return pulseMultipliers;
    }

    public void setPulseMultipliers(BigDecimal[] pulseMultipliers) {
        this.pulseMultipliers = pulseMultipliers;
    }

    public Unit[] getDisplayUnits() {
        return displayUnits;
    }

    public void setDisplayUnits(Unit[] displayUnits) {
        this.displayUnits = displayUnits;
    }


} // public class RealTimeBasePage extends AbstractBasePage
