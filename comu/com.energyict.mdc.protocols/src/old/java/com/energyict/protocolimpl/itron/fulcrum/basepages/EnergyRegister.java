/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EnergyRegister.java
 *
 * Created on 13 september 2006, 13:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class EnergyRegister {

    private BigDecimal totalValue;
    private BigDecimal[] valueRates;

    /** Creates a new instance of EnergyRegister */
    public EnergyRegister(byte[] data, int offset) throws IOException {

        totalValue = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        valueRates = new BigDecimal[RegisterFactory.MAX_NR_OF_RATES];
        for (int i=0;i<RegisterFactory.MAX_NR_OF_RATES;i++) {
            valueRates[i] = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));offset+=4;
        }
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("EnergyRegister:\n");
        strBuff.append("   totalValue="+getTotalValue()+"\n");
        for (int i=0;i<getValueRates().length;i++) {
            strBuff.append("       valueRates["+i+"]="+getValueRates()[i]+"\n");
        }
        return strBuff.toString();
    }

    public static int size() {
        return 5*4+4+4; // * extra unused 2*4 bytes
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    private void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal[] getValueRates() {
        return valueRates;
    }

    private void setValueRates(BigDecimal[] valueRates) {
        this.valueRates = valueRates;
    }

}
