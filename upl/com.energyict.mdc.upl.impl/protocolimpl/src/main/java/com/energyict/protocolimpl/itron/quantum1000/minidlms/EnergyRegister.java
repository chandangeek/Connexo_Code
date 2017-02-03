/*
 * Result.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class EnergyRegister {
    
    private QuantityId quantityId;
    private BigDecimal multiplier;
    private int rateSchedule;
    
    /** Creates a new instance of Result */
    public EnergyRegister(byte[] data, int offset, MeterSetup meterSetup) throws IOException {
        setQuantityId(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        
        // Multiplier ID
        // 0 = 1.0
        // 1 = CT value
        // 2 = PT value
        // 3 = CT*PT value
        // 4 = customized value
        int multiplierId = ProtocolUtils.getInt(data,offset++,1);
        switch(multiplierId) {
            case 0: 
                setMultiplier(new BigDecimal("1.0"));
                break;
            case 1: 
                setMultiplier(meterSetup.getCtMultiplier());
                break;
            case 2: 
                setMultiplier(meterSetup.getPtMultiplier());
                break;
            case 3: 
                setMultiplier(meterSetup.getCtMultiplier().multiply(meterSetup.getPtMultiplier()));
                break;
            case 4: 
                setMultiplier(meterSetup.getCustomMultiplier());
                break;
        }
        
        setRateSchedule(ProtocolUtils.getInt(data,offset++,1));        
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterConfig:\n");
        strBuff.append("   multiplier="+getMultiplier()+"\n");
        strBuff.append("   quantityId="+getQuantityId()+"\n");
        strBuff.append("   rateSchedule="+getRateSchedule()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
        return 4;
    }
    
    public QuantityId getQuantityId() {
        return quantityId;
    }

    public void setQuantityId(QuantityId quantityId) {
        this.quantityId = quantityId;
    }



    public int getRateSchedule() {
        return rateSchedule;
    }

    public void setRateSchedule(int rateSchedule) {
        this.rateSchedule = rateSchedule;
    }    

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    
}
