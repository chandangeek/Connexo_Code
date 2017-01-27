/*
 * CumulativeMaximumDemand.java
 *
 * Created on 19 maart 2004, 12:57
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class CumulativeMaximumDemand extends MeterReadingsBlockImpl {
   
	private int regId;
	private int regIdEnergyIndex;
	private int regIdRegisterNumber;
	private int currentCMD;
	private int billingCMD;
    
    /** Creates a new instance of GeneralInformation */
    public CumulativeMaximumDemand(byte[] data) {
        super(data);
    }
    
    public String print() {
        return "REG_ID=0x"+Integer.toHexString(getRegId())+
               " (ENERGY_INDEX="+getRegIdEnergyIndex()+
               ", REGISTER_NUMBER="+getRegIdRegisterNumber()+
               "), CURRENT_CMD="+getCurrentCMD()+
               ", BILLING_CMD="+getBillingCMD();
    }
    
    protected void parse() throws IOException {
        setRegId(ProtocolUtils.byte2int(getData()[1]));
        setRegIdEnergyIndex(getRegId()>>5);
        setRegIdRegisterNumber(getRegId()&0x1F);
        setCurrentCMD(ProtocolUtils.getIntLE(getData(),2,3));
        setBillingCMD(ProtocolUtils.getIntLE(getData(),5,3));
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
    
    /** Getter for property currentCMD.
     * @return Value of property currentCMD.
     *
     */
    public int getCurrentCMD() {
        return currentCMD;
    }
    
    /** Setter for property currentCMD.
     * @param currentCMD New value of property currentCMD.
     *
     */
    public void setCurrentCMD(int currentCMD) {
        this.currentCMD = currentCMD;
    }
    
    /** Getter for property billingCMD.
     * @return Value of property billingCMD.
     *
     */
    public int getBillingCMD() {
        return billingCMD;
    }
    
    /** Setter for property billingCMD.
     * @param billingCMD New value of property billingCMD.
     *
     */
    public void setBillingCMD(int billingCMD) {
        this.billingCMD = billingCMD;
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
