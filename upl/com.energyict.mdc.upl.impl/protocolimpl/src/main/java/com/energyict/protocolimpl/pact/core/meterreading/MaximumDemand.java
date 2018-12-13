/*
 * MaximumDemand.java
 *
 * Created on 19 maart 2004, 13:13
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocolimpl.utils.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class MaximumDemand extends MeterReadingsBlockImpl {
    
	private int regId;
	private int regIdEnergyIndex;
	private int regIdRegisterNumber;
	private int currentMD;
	private int billingMD;
	private int trg;
	private int bTrg;
    
	private char type;
    
    /** Creates a new instance of MaximumDemand */
    public MaximumDemand(byte[] data,char type) {
        super(data);
        this.type=type;
    }

    public boolean isType_m() {
        return type == 'm';
    }
    
    public boolean isType_q() {
        return type == 'q';
    }
    
    public String print() {
        return "REG_ID=0x"+Integer.toHexString(getRegId())+
               " (ENERGY_INDEX="+getRegIdEnergyIndex()+
               ", REGISTER_NUMBER="+getRegIdRegisterNumber()+
               "), CURRENT_MD="+getCurrentMD()+
               ", BILLING_MD="+(getBillingMD()==0xFFFF ? "N/A":Integer.toString(getBillingMD()))+
               ", TRG="+getTrg()+
               ", B_TRG="+getBTrg();
    }
    
    protected void parse() throws java.io.IOException {
        setRegId(ProtocolUtils.byte2int(getData()[1]));
        setRegIdEnergyIndex(getRegId()>>5);
        setRegIdRegisterNumber(getRegId()&0x1F);
        setCurrentMD(ProtocolUtils.getIntLE(getData(),2,2));
        setBillingMD(ProtocolUtils.getIntLE(getData(),4,2));
        setTrg(ProtocolUtils.byte2int(getData()[6]));
        setBTrg(ProtocolUtils.byte2int(getData()[7]));
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
    
    /** Getter for property currentMD.
     * @return Value of property currentMD.
     *
     */
    public int getCurrentMD() {
        return currentMD;
    }
    
    /** Setter for property currentMD.
     * @param currentMD New value of property currentMD.
     *
     */
    public void setCurrentMD(int currentMD) {
        this.currentMD = currentMD;
    }
    
    /** Getter for property billingMD.
     * @return Value of property billingMD.
     *
     */
    public int getBillingMD() {
        return billingMD;
    }
    
    /** Setter for property billingMD.
     * @param billingMD New value of property billingMD.
     *
     */
    public void setBillingMD(int billingMD) {
        this.billingMD = billingMD;
    }
    
    /** Getter for property trg.
     * @return Value of property trg.
     *
     */
    public int getTrg() {
        return trg;
    }
    
    /** Setter for property trg.
     * @param trg New value of property trg.
     *
     */
    public void setTrg(int trg) {
        this.trg = trg;
    }
    
    /** Getter for property bTrg.
     * @return Value of property bTrg.
     *
     */
    public int getBTrg() {
        return bTrg;
    }
    
    /** Setter for property bTrg.
     * @param bTrg New value of property bTrg.
     *
     */
    public void setBTrg(int bTrg) {
        this.bTrg = bTrg;
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
    
    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public char getType() {
        return type;
    }
    
}
