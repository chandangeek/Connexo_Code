/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterIdentitySerialNumber.java
 *
 * Created on 22 maart 2004, 10:48
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class MeterIdentitySerialNumber {

    int io;
    int rangeAndClass;
    int range;
    int meterClass;
    int diAndFac;
    int di;
    int meterFactor;
    int iPrimary;
    int vPrimary;

    byte[] data;

    boolean mw=false;
    double voltage;
    int current;
    MeterType meterType;

    /** Creates a new instance of MeterIdentitySerialNumber */
    public MeterIdentitySerialNumber(byte[] data) {
    	if(data != null){
    		this.data=data;
    	}
        parse();
    }

    public String toString() {
       return "IO=0x"+Integer.toHexString(getIo())+", R&C=0x"+Integer.toHexString(getRangeAndClass())+", (RANGE="+getRange()+", CLASS="+getMeterClass()+"),MT="+getMeterType().getType()+" ("+getMeterType()+"), D&F=0x"+Integer.toHexString(getDiAndFac())+", (DI="+getDi()+", METERFACTOR="+getMeterFactor()+"), I_PRIMARY="+getIPrimary()+", V_PRIMARY="+getVPrimary()+", current="+getCurrent()+", voltage="+getVoltage()+", mW commissioned="+isMw();
    }

    private void parse() {
        try {
            setIo(ProtocolUtils.byte2int(data[2]));
            setRangeAndClass(ProtocolUtils.byte2int(data[3]));
            setRange(getRangeAndClass()>>4);
            setMeterClass(getRangeAndClass()&0x0F);
            setMeterType(MeterType.getMeterType(ProtocolUtils.byte2int(data[4])));
            setDiAndFac(ProtocolUtils.byte2int(data[5]));
            setDi(getDiAndFac()>>4);
            setMeterFactor((getDiAndFac()&0x0F)-6);
            setIPrimary(ProtocolUtils.getBCD2IntLE(data,6,2));
            setVPrimary(ProtocolUtils.getBCD2IntLE(data,8,2));
            setCurrent(getIPrimary());
            setVoltage(getVPrimary());
            // process voltage
            if (getVPrimary() == 0) {
				setVoltage(getMeterType().getMeasuredVoltage());
			}
            int exp = (getVPrimary() / 1000) - 11;
            if (exp >= -1) {
                double multiplier = Math.pow(10,exp);
                setVoltage((int)((getVPrimary()%1000) * multiplier));
            } else {
				setVoltage(getVPrimary());
			}

            double maxPower = getMeterType().getMultiplier() * getVoltage() * getCurrent();
            if (maxPower > 1000000) {
                setMw(true);
            }
            else {
                setMw(false);
            }
        }
        catch(IOException e) {
            e.printStackTrace(); // should never happen
        }
    }

    /** Getter for property io.
     * @return Value of property io.
     *
     */
    public int getIo() {
        return io;
    }

    /** Setter for property io.
     * @param io New value of property io.
     *
     */
    public void setIo(int io) {
        this.io = io;
    }

    /** Getter for property rangeAndClass.
     * @return Value of property rangeAndClass.
     *
     */
    public int getRangeAndClass() {
        return rangeAndClass;
    }

    /** Setter for property rangeAndClass.
     * @param rangeAndClass New value of property rangeAndClass.
     *
     */
    public void setRangeAndClass(int rangeAndClass) {
        this.rangeAndClass = rangeAndClass;
    }



    /** Getter for property diAndFac.
     * @return Value of property diAndFac.
     *
     */
    public int getDiAndFac() {
        return diAndFac;
    }

    /** Setter for property diAndFac.
     * @param diAndFac New value of property diAndFac.
     *
     */
    public void setDiAndFac(int diAndFac) {
        this.diAndFac = diAndFac;
    }

    /** Getter for property meterFactor.
     * @return Value of property meterFactor.
     *
     */
    public int getMeterFactor() {
        return meterFactor;
    }

    /** Setter for property meterFactor.
     * @param meterFactor New value of property meterFactor.
     *
     */
    public void setMeterFactor(int meterFactor) {
        this.meterFactor = meterFactor;
    }

    /** Getter for property iPrimary.
     * @return Value of property iPrimary.
     *
     */
    public int getIPrimary() {
        return iPrimary;
    }

    /** Setter for property iPrimary.
     * @param iPrimary New value of property iPrimary.
     *
     */
    public void setIPrimary(int iPrimary) {
        this.iPrimary = iPrimary;
    }

    /** Getter for property vPrimary.
     * @return Value of property vPrimary.
     *
     */
    public int getVPrimary() {
        return vPrimary;
    }

    /** Setter for property vPrimary.
     * @param vPrimary New value of property vPrimary.
     *
     */
    public void setVPrimary(int vPrimary) {
        this.vPrimary = vPrimary;
    }

    /** Getter for property di.
     * @return Value of property di.
     *
     */
    public int getDi() {
        return di;
    }

    /** Setter for property di.
     * @param di New value of property di.
     *
     */
    public void setDi(int di) {
        this.di = di;
    }

    /** Getter for property range.
     * @return Value of property range.
     *
     */
    public int getRange() {
        return range;
    }

    /** Setter for property range.
     * @param range New value of property range.
     *
     */
    public void setRange(int range) {
        this.range = range;
    }

    /** Getter for property meterClass.
     * @return Value of property meterClass.
     *
     */
    public int getMeterClass() {
        return meterClass;
    }

    /** Setter for property meterClass.
     * @param meterClass New value of property meterClass.
     *
     */
    public void setMeterClass(int meterClass) {
        this.meterClass = meterClass;
    }

    /** Getter for property mw.
     * @return Value of property mw.
     *
     */
    public boolean isMw() {
        return mw;
    }

    /** Setter for property mw.
     * @param mw New value of property mw.
     *
     */
    public void setMw(boolean mw) {
        this.mw = mw;
    }



    /** Getter for property current.
     * @return Value of property current.
     *
     */
    public int getCurrent() {
        return current;
    }

    /** Setter for property current.
     * @param current New value of property current.
     *
     */
    public void setCurrent(int current) {
        this.current = current;
    }

    /** Getter for property meterType.
     * @return Value of property meterType.
     *
     */
    public com.energyict.protocolimpl.pact.core.common.MeterType getMeterType() {
        return meterType;
    }

    /** Setter for property meterType.
     * @param meterType New value of property meterType.
     *
     */
    public void setMeterType(com.energyict.protocolimpl.pact.core.common.MeterType meterType) {
        this.meterType = meterType;
    }

    /** Getter for property voltage.
     * @return Value of property voltage.
     *
     */
    public double getVoltage() {
        return voltage;
    }

    /** Setter for property voltage.
     * @param voltage New value of property voltage.
     *
     */
    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

}
