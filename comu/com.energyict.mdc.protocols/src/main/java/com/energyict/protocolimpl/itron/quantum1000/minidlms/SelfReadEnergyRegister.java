/*
 * SelfReadEnergyRegister.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SelfReadEnergyRegister {


    private QuantityId qid; // QUANTITY_ID,
    private double integralValue; // DOUBLE,
    private double fractionalValue; // DOUBLE.

    /**
     * Creates a new instance of SelfReadEnergyRegister
     */
    public SelfReadEnergyRegister(byte[] data,int offset) throws IOException {
        setQid(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        setIntegralValue(Double.longBitsToDouble(ProtocolUtils.getLong(data,offset)));
        offset+=8;
        setFractionalValue(Double.longBitsToDouble(ProtocolUtils.getLong(data,offset)));
        offset+=8;

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadEnergyRegister:\n");
        strBuff.append("   fractionalValue="+getFractionalValue()+"\n");
        strBuff.append("   integralValue="+getIntegralValue()+"\n");
        strBuff.append("   qid="+getQid()+"\n");
        return strBuff.toString();
    }

    static public int size() {
        return 18;
    }

    public QuantityId getQid() {
        return qid;
    }

    public void setQid(QuantityId qid) {
        this.qid = qid;
    }

    public double getIntegralValue() {
        return integralValue;
    }

    public void setIntegralValue(double integralValue) {
        this.integralValue = integralValue;
    }

    public double getFractionalValue() {
        return fractionalValue;
    }

    public void setFractionalValue(double fractionalValue) {
        this.fractionalValue = fractionalValue;
    }



}
