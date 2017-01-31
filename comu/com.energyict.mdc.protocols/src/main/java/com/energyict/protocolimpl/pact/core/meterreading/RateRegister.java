/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RateRegister.java
 *
 * Created on 19 maart 2004, 13:30
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class RateRegister extends MeterReadingsBlockImpl {

    private int regId;
    private int regIdEnergyIndex;
    private int regIdRegisterNumber;
    private int currentValue;
    private int billingValue;

    /** Creates a new instance of RateRegister */
    public RateRegister(byte[] data) {
        super(data);
    }

    public String print() {
        return "REG_ID=0x"+Integer.toHexString(getRegId())+
               " (ENERGY_INDEX="+getRegIdEnergyIndex()+
               ", REGISTER_NUMBER="+getRegIdRegisterNumber()+
               "), CURRENT_VALUE="+getCurrentValue()+
               ", BILLING_VALUE="+(getBillingValue()==0xFFFFFF ? "N/A":Integer.toString(getBillingValue()));
    }

    protected void parse() throws java.io.IOException {
        setRegId(ProtocolUtils.byte2int(getData()[1]));
        setRegIdEnergyIndex(getRegId()>>5);
        setRegIdRegisterNumber(getRegId()&0x1F);
        setCurrentValue(ProtocolUtils.getIntLE(getData(),2,3));
        setBillingValue(ProtocolUtils.getIntLE(getData(),5,3));
    }

    /** Getter for property regId.
     * @return Value of property regId.
     *
     */
    public int getRegId() {
        return regId;
    }

    /** Setter for property regId.
     * @param regId New value of property regId.
     *
     */
    public void setRegId(int regId) {
        this.regId = regId;
    }

    /** Getter for property currentValue.
     * @return Value of property currentValue.
     *
     */
    public int getCurrentValue() {
        return currentValue;
    }

    /** Setter for property currentValue.
     * @param currentValue New value of property currentValue.
     *
     */
    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    /** Getter for property billingValue.
     * @return Value of property billingValue.
     *
     */
    public int getBillingValue() {
        return billingValue;
    }

    /** Setter for property billingValue.
     * @param billingValue New value of property billingValue.
     *
     */
    public void setBillingValue(int billingValue) {
        this.billingValue = billingValue;
    }

    /** Getter for property regIdEnergyIndex.
     * @return Value of property regIdEnergyIndex.
     *
     */
    public int getRegIdEnergyIndex() {
        return regIdEnergyIndex;
    }

    /** Setter for property regIdEnergyIndex.
     * @param regIdEnergyIndex New value of property regIdEnergyIndex.
     *
     */
    public void setRegIdEnergyIndex(int regIdEnergyIndex) {
        this.regIdEnergyIndex = regIdEnergyIndex;
    }

    /** Getter for property regIdRegisterNumber.
     * @return Value of property regIdRegisterNumber.
     *
     */
    public int getRegIdRegisterNumber() {
        return regIdRegisterNumber;
    }

    /** Setter for property regIdRegisterNumber.
     * @param regIdRegisterNumber New value of property regIdRegisterNumber.
     *
     */
    public void setRegIdRegisterNumber(int regIdRegisterNumber) {
        this.regIdRegisterNumber = regIdRegisterNumber;
    }

}
