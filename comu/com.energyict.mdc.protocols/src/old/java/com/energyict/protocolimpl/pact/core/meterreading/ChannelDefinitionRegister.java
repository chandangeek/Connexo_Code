/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ChannelDefinitionRegister.java
 *
 * Created on 19 maart 2004, 15:14
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;

import java.math.BigDecimal;
/**
 *
 * @author  Koen
 */
public class ChannelDefinitionRegister extends MeterReadingsBlockImpl {

    public static final int INTERNAL_UNIT_REGISTER=0;
    public static final int PHASE_1=1;
    public static final int PHASE_2=2;
    public static final int PHASE_3=3;
    public static final int EXTERNAL_REGISTER=4;
    public static final int FORWARDED_ACTIVE_ENERGY=5;
    public static final int EXCESS_POWER_INTEGRATION=6;
    public static final int NOT_SPECIFIED=255;

    private int channelId;
    private int bpIndex;
    private int channelNumber;
    private int channelType;
    private int registerValue;
    private int eType;
    private int fac;
    private int meterFactorExp;

    /** Creates a new instance of ChannelDefinitionRegister */
    public ChannelDefinitionRegister(byte[] data) {
        super(data,true);
    }

    public String print() {
       return "CHN_ID=0x"+Integer.toHexString(getChannelId())+" (BP_INDEX="+getBpIndex()+", CHAN_NUM="+getChannelNumber()+
              "), CHN_TYP="+getChannelType()+", REG_VALUE="+getRegisterValue()+", E_TYPE=0x"+Integer.toHexString(getEType())+" ("+EnergyTypeCode.getUnit(getEType(),true)+")"+
              ", FAC="+getFac()+" (METERFACTOR="+getMeterFactorExp()+")";
    }

    protected void parse() throws java.io.IOException {
       setChannelId(ProtocolUtils.byte2int(getData()[1]));
       setBpIndex(getChannelId()>>4);
       setChannelNumber(getChannelId()&0x0F);
       setChannelType(ProtocolUtils.byte2int(getData()[2]));
       setRegisterValue(ProtocolUtils.getIntLE(getData(),3,3));
       setEType(ProtocolUtils.byte2int(getData()[6]));
       setFac(ProtocolUtils.byte2int(getData()[7]));
       setMeterFactorExp(getFac()-48);
    }

    /** Getter for property channelId.
     * @return Value of property channelId.
     *
     */
    public int getChannelId() {
        return channelId;
    }

    /** Setter for property channelId.
     * @param channelId New value of property channelId.
     *
     */
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    /** Getter for property bpIndex.
     * @return Value of property bpIndex.
     *
     */
    public int getBpIndex() {
        return bpIndex;
    }

    /** Setter for property bpIndex.
     * @param bpIndex New value of property bpIndex.
     *
     */
    public void setBpIndex(int bpIndex) {
        this.bpIndex = bpIndex;
    }

    /** Getter for property channelNumber.
     * @return Value of property channelNumber.
     *
     */
    public int getChannelNumber() {
        return channelNumber;
    }

    /** Setter for property channelNumber.
     * @param channelNumber New value of property channelNumber.
     *
     */
    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    /** Getter for property channelType.
     * @return Value of property channelType.
     *
     */
    public int getChannelType() {
        return channelType;
    }

    /** Setter for property channelType.
     * @param channelType New value of property channelType.
     *
     */
    public void setChannelType(int channelType) {
        this.channelType = channelType;
    }

    /** Getter for property registerValue.
     * @return Value of property registerValue.
     *
     */
    public int getRegisterValue() {
        return registerValue;
    }

    /** Setter for property registerValue.
     * @param registerValue New value of property registerValue.
     *
     */
    public void setRegisterValue(int registerValue) {
        this.registerValue = registerValue;
    }

    /** Getter for property eType.
     * @return Value of property eType.
     *
     */
    public int getEType() {
        return eType;
    }

    /** Setter for property eType.
     * @param eType New value of property eType.
     *
     */
    public void setEType(int eType) {
        this.eType = eType;
    }

    /** Getter for property fac.
     * @return Value of property fac.
     *
     */
    public int getFac() {
        return fac;
    }

    /** Setter for property fac.
     * @param fac New value of property fac.
     *
     */
    public void setFac(int fac) {
        this.fac = fac;
    }

    /** Getter for property meterFactorExp.
     * @return Value of property meterFactorExp.
     *
     */
    public int getMeterFactorExp() {
        return meterFactorExp;
    }

    public BigDecimal getMeterFactor() {
       return new BigDecimal(Math.pow(10, getMeterFactorExp()));
    }

    /** Setter for property meterFactorExp.
     * @param meterFactorExp New value of property meterFactorExp.
     *
     */
    public void setMeterFactorExp(int meterFactorExp) {
        this.meterFactorExp = meterFactorExp;
    }

}
