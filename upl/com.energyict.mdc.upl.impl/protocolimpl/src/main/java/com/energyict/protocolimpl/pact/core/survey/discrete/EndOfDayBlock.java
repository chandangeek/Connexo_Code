/*
 * DiscreteEOD.java
 *
 * Created on 11 maart 2004, 13:15
 */

package com.energyict.protocolimpl.pact.core.survey.discrete;

import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;
import com.energyict.protocolimpl.pact.core.common.PactUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Calendar;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class EndOfDayBlock {
    
    private static final int DEBUG=0;
    
    private Calendar date; // always in GMT
    private int flags;
    private int flagsExp;
    private int flagsDi;
    private boolean flagsSGN;
    private int register;
    private int etype;
    private int authentification;
    private boolean valid;
    private boolean closed;
    private int meterFactorExp;
    private int valueType;
    private int divisor;
    
    private byte[] data;
    private final int[] DI = {2,5,10,1}; 
    private TimeZone timeZone;
    
    /** Creates a new instance of DiscreteEOD */
    public EndOfDayBlock(byte[] data,TimeZone timeZone) {
    	if(data != null){
    		this.data=data.clone();
    	}
        this.timeZone=timeZone;
        setValid(true);
        parse();
    }
    
    public String toString() {
        return "DATE="+getDate().getTime()+", FLAGS=0x"+Integer.toHexString(getFlags())+" (EXP="+getFlagsExp()+", DI="+getFlagsDi()+", SGN="+isFlagsSGN()+"), REGISTER="+getRegister()+", ETYPE=0x"+Integer.toHexString(getEtype())+" ("+EnergyTypeCode.getUnit(getEtype(),true)+"), AUTHENT="+getAuthentification();
    }
    
    private void parse() {
        //**********************************************************************************
        // common
        // parse DATE and check if invalid 
        int iDate = ProtocolUtils.byte2int(data[0])+ProtocolUtils.byte2int(data[1])*256;
        if (((iDate & 0xFE00) == 0xFE00) || ((iDate & 0x01FF) == 0x01FF)) {
			setValid(false);
		}
        
        if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> EndOfDayblock, iDate="+iDate);
		}
        setDate(PactUtils.getCalendar(iDate,0,timeZone));

        setFlags(ProtocolUtils.byte2int(data[2]));
        
        // parse REGISTER and check if invalid
        setRegister(ProtocolUtils.byte2int(data[3])+ProtocolUtils.byte2int(data[4])*256); 
        if (getRegister() == 0xFFFF) {
			setValid(false);
		}
        if (getRegister() == 0xFFFE) {
			setClosed(false);
		} else {
			setClosed(true);
		}
        
        setEtype(ProtocolUtils.byte2int(data[5]));
        setAuthentification(ProtocolUtils.byte2int(data[6])+ProtocolUtils.byte2int(data[7])*256);         
        
        //**********************************************************************************
        // in case of ETYPE for electrical, non elektrical and flag parameters
        setFlagsExp((getFlags()>>4)&0x0F);
        if (getFlagsExp() <= 5) {
			setMeterFactorExp(getFlagsExp());
		}
        if (getFlagsExp() >= 13) {
			setMeterFactorExp(getFlagsExp()-16);
		}
        setFlagsDi(DI[(getFlags()>>2)&0x03]);
        setFlagsSGN((getFlags() & 0x1) == 0x1);      
        
        //**********************************************************************************
        // in case of ETYPE for instanteneous parameters
        setValueType((getFlags()>>4)&0x0F);
        setDivisor(getFlags()&0x0F);
    }
    
    /** Getter for property date.
     * @return Value of property date.
     *
     */
    public java.util.Calendar getDate() {
        return date;
    }
    
    /** Setter for property date.
     * @param date New value of property date.
     *
     */
    public void setDate(java.util.Calendar date) {
        this.date = date;
    }
    
    /** Getter for property flags.
     * @return Value of property flags.
     *
     */
    public int getFlags() {
        return flags;
    }
    
    /** Setter for property flags.
     * @param flags New value of property flags.
     *
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }
    
    /** Getter for property register.
     * @return Value of property register.
     *
     */
    public int getRegister() {
        return register;
    }
    
    /** Setter for property register.
     * @param register New value of property register.
     *
     */
    public void setRegister(int register) {
        this.register = register;
    }
    
    /** Getter for property etype.
     * @return Value of property etype.
     *
     */
    public int getEtype() {
        return etype;
    }
    
    /** Setter for property etype.
     * @param etype New value of property etype.
     *
     */
    public void setEtype(int etype) {
        this.etype = etype;
    }
    
    /** Getter for property authentification.
     * @return Value of property authentification.
     *
     */
    public int getAuthentification() {
        return authentification;
    }
    
    /** Setter for property authentification.
     * @param authentification New value of property authentification.
     *
     */
    public void setAuthentification(int authentification) {
        this.authentification = authentification;
    }
    
    /** Getter for property valid.
     * @return Value of property valid.
     *
     */
    public boolean isValid() {
        return valid;
    }
    
    /** Setter for property valid.
     * @param valid New value of property valid.
     *
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    /** Getter for property flagsExp.
     * @return Value of property flagsExp.
     *
     */
    public int getFlagsExp() {
        return flagsExp;
    }
    
    /** Setter for property flagsExp.
     * @param flagsExp New value of property flagsExp.
     *
     */
    public void setFlagsExp(int flagsExp) {
        this.flagsExp = flagsExp;
    }
    
    /** Getter for property flagsDiCode.
     * @return Value of property flagsDiCode.
     *
     */
    public int getFlagsDi() {
        return flagsDi;
    }
    
    /** Setter for property flagsDiCode.
     * @param flagsDiCode New value of property flagsDiCode.
     *
     */
    public void setFlagsDi(int flagsDi) {
        this.flagsDi = flagsDi;
    }
    
    /** Getter for property flagsSGN.
     * @return Value of property flagsSGN.
     *
     */
    public boolean isFlagsSGN() {
        return flagsSGN;
    }
    
    /** Setter for property flagsSGN.
     * @param flagsSGN New value of property flagsSGN.
     *
     */
    public void setFlagsSGN(boolean flagsSGN) {
        this.flagsSGN = flagsSGN;
    }
    
    /** Getter for property meterFactorExp.
     * @return Value of property meterFactorExp.
     *
     */
    public int getMeterFactorExp() {
        return meterFactorExp;
    }
    
    /** Setter for property meterFactorExp.
     * @param meterFactorExp New value of property meterFactorExp.
     *
     */
    public void setMeterFactorExp(int meterFactorExp) {
        this.meterFactorExp = meterFactorExp;
    }
    
    /** Getter for property closed.
     * @return Value of property closed.
     *
     */
    public boolean isClosed() {
        return closed;
    }
    
    /** Setter for property closed.
     * @param closed New value of property closed.
     *
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    /** Getter for property valueType.
     * @return Value of property valueType.
     *
     */
    public int getValueType() {
        return valueType;
    }
    
    /** Setter for property valueType.
     * @param valueType New value of property valueType.
     *
     */
    public void setValueType(int valueType) {
        this.valueType = valueType;
    }
    
    /** Getter for property divisor.
     * @return Value of property divisor.
     *
     */
    public int getDivisor() {
        return divisor;
    }
    
    /** Setter for property divisor.
     * @param divisor New value of property divisor.
     *
     */
    public void setDivisor(int divisor) {
        this.divisor = divisor;
    }
    
}
