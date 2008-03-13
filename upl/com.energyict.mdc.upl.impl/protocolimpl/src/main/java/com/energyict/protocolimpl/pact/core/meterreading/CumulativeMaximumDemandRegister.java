/*
 * CumulativeMaximumDemandRegister.java
 *
 * Created on 19 maart 2004, 16:00
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import java.io.*;
import com.energyict.protocol.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class CumulativeMaximumDemandRegister extends MeterReadingsBlockImpl {
    int channelId;
    int bpIndex;
    int channelNumber;
    int tReg;
    int registerNumber;
    int registerValue;
    
    /** Creates a new instance of CumulativeMaximumDemandRegister */
    public CumulativeMaximumDemandRegister(byte[] data) {
        super(data,true);
    }

    public String print() {
       return "CHN_ID=0x"+Integer.toHexString(getChannelId())+" (BP_INDEX="+getBpIndex()+", CHAN_NUM="+getChannelNumber()+
              "), T_REG=0x"+Integer.toHexString(getTReg())+" (TRIGGER_CHAN=unused, REGISTER_NUM="+getRegisterNumber()+
              "), REG_VALUE="+getRegisterValue(); 
    }
    
    protected void parse() throws java.io.IOException {
       setChannelId(ProtocolUtils.byte2int(getData()[1]));
       setBpIndex(getChannelId()>>4);
       setChannelNumber(getChannelId()&0x0F);
       setTReg(ProtocolUtils.byte2int(getData()[2]));
       setRegisterNumber(getTReg()&0x0F); 
       setRegisterValue(ProtocolUtils.getIntLE(getData(),3,3));        
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
    
    /** Getter for property tReg.
     * @return Value of property tReg.
     *
     */
    public int getTReg() {
        return tReg;
    }
    
    /** Setter for property tReg.
     * @param tReg New value of property tReg.
     *
     */
    public void setTReg(int tReg) {
        this.tReg = tReg;
    }
    
   
    /** Getter for property registerNumber.
     * @return Value of property registerNumber.
     *
     */
    public int getRegisterNumber() {
        return registerNumber;
    }
    
    /** Setter for property registerNumber.
     * @param registerNumber New value of property registerNumber.
     *
     */
    public void setRegisterNumber(int registerNumber) {
        this.registerNumber = registerNumber;
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
    
}
