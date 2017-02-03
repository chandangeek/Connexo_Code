/*
 * EnergyRegisterValue.java
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

/**
 *
 * @author Koen
 */
public class EnergyRegisterValue {
    
    private QuantityId qid; // QUANTITY_ID,
    private double totalReg; // DOUBLE,
    
    /** Creates a new instance of EnergyRegisterValue */
    public EnergyRegisterValue(byte[] data,int offset) throws IOException {
        setQid(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        setTotalReg(Double.longBitsToDouble(ProtocolUtils.getLong(data,offset)));
        offset+=4;
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EnergyRegisterValue:\n");
        strBuff.append("   qid="+getQid()+"\n");
        strBuff.append("   totalReg="+getTotalReg()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
        return 10;
    }

    public QuantityId getQid() {
        return qid;
    }

    public void setQid(QuantityId qid) {
        this.qid = qid;
    }

    public double getTotalReg() {
        return totalReg;
    }

    public void setTotalReg(double totalReg) {
        this.totalReg = totalReg;
    }
    

    
}
