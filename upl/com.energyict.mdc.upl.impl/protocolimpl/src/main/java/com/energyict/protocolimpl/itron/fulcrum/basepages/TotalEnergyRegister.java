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

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class TotalEnergyRegister {
    
    private BigDecimal totalValue;
    private BigDecimal[] valueRates;
    
    /** Creates a new instance of EnergyRegister */
    public TotalEnergyRegister(byte[] data, int offset) throws IOException {
        totalValue = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        valueRates = new BigDecimal[RegisterFactory.MAX_NR_OF_RATES];
        for (int i=0;i<RegisterFactory.MAX_NR_OF_RATES;i++) {
            valueRates[i] = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));offset+=4;
        }
    } // public TotalEnergyRegister(byte[] data, int offset) throws IOException 
    
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EnergyRegister:\n");
        strBuff.append("   totalValue="+getTotalValue()+"\n");
        for (int i=0;i<getValueRates().length;i++) {
            strBuff.append("       valueRates["+i+"]="+getValueRates()[i]+"\n");
        }
        return strBuff.toString();
    }
    
    static public int size() {
        return 5*4; // * extra unused 2*4 bytes
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
