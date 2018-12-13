/*
 * MaximumDemandRegister.java
 *
 * Created on 19 maart 2004, 15:48
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocolimpl.pact.core.common.PactUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class MaximumDemandRegister extends MeterReadingsBlockImpl {
    
	private int channelId;
	private int bpIndex;
	private int channelNumber;
	private int tReg;
	private int triggerChannel;
	private int registerNumber;
	private int registerValue;
	private Date dateTime;
    
    /** Creates a new instance of MaximumDemandRegister */
    public MaximumDemandRegister(byte[] data, TimeZone timeZone) {
        super(data,true,timeZone);
    }
    
    public String print() {
       return "CHN_ID=0x"+Integer.toHexString(getChannelId())+" (BP_INDEX="+getBpIndex()+", CHAN_NUM="+getChannelNumber()+
              "), T_REG=0x"+Integer.toHexString(getTReg())+" (TRIGGER_CHAN="+getTriggerChannel()+", REGISTER_NUM="+getRegisterNumber()+
              "), REG_VALUE="+getRegisterValue()+", date&time="+getDateTime(); 
    }

    
    protected void parse() throws java.io.IOException {
       setChannelId(ProtocolUtils.byte2int(getData()[1]));
       setBpIndex(getChannelId()>>4);
       setChannelNumber(getChannelId()&0x0F);
       setTReg(ProtocolUtils.byte2int(getData()[2]));
       setTriggerChannel(getTReg()>>4);
       setRegisterNumber(getTReg()&0x0F); 
       setRegisterValue(ProtocolUtils.getIntLE(getData(),3,2));
       setDateTime(PactUtils.getCalendar(ProtocolUtils.getIntLE(getData(),5,3),getTimeZone()).getTime());
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
    
    /** Getter for property triggerChannel.
     * @return Value of property triggerChannel.
     *
     */
    public int getTriggerChannel() {
        return triggerChannel;
    }
    
    /** Setter for property triggerChannel.
     * @param triggerChannel New value of property triggerChannel.
     *
     */
    public void setTriggerChannel(int triggerChannel) {
        this.triggerChannel = triggerChannel;
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
    
    /** Getter for property dateTime.
     * @return Value of property dateTime.
     *
     */
    public Date getDateTime() {
        return dateTime;
    }
    
    /** Setter for property dateTime.
     * @param dateTime New value of property dateTime.
     *
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
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
