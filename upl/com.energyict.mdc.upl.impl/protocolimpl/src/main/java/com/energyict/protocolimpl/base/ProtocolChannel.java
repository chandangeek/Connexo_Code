/*
 * ProtocolChannel.java
 *
 * Created on 12 september 2003, 9:53
 */

package com.energyict.protocolimpl.base;

import java.io.*;
import java.math.*;
import java.util.*;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

/**
 *
 * @author  Koen
 * A channel has a (register) code.
 * Use as a registercode is only a hint! With the channelmap, various configurations can be mapped.
 * E.g. 1.0.3+7 where
 *      1.0.3 is the register code
 *      +7 tells the channel is cumulative and has a 7 digit wrap around value 0000000 -> 9999999 decimal!
 */
public class ProtocolChannel {
    
    String register=null;
    boolean cumul=false;
    BigDecimal wrapAroundValue=null;
    
    /** Creates a new instance of Channel */
    public ProtocolChannel(String strChannel) throws InvalidPropertyException {
        int index=0;
        if ((index=strChannel.indexOf("+")) != -1) {
            cumul = true;
            register = strChannel.substring(0,strChannel.indexOf("+"));
            if (index == (strChannel.length()-1)) throw new InvalidPropertyException("Error in ChannelMap property! Nr of digits must be set for a cumul channel. Register must be followed by '+' sign followed by nr of digits (e.g. 1.0.3+7)");
            int digits = Integer.parseInt(strChannel.substring(index+1,strChannel.length()));
            // tricky, if digits is small (<100) then wrap around is an exponent to 10, if it is big (> 100) then wraparount uses the value of digits itself
            if (digits<100)
                wrapAroundValue = BigDecimal.valueOf(1).movePointRight(digits); //new BigDecimal(Math.pow(10, digits)).;
            else
                wrapAroundValue = new BigDecimal(""+digits);  
        }
        else 
            register = strChannel;
    }
    
    
    public String toString() {
       return "ProtocolChannel: register="+register+", cumul="+cumul+", wrapAroundValue="+wrapAroundValue;    
    }
    
    public String getRegister() {
        return register;
    }
    
    public int getValue() {
        return Integer.parseInt(register);
    }
    
    public int getIntValue(int index) {
        return Integer.parseInt(getValue(index));
    }
    
    public int getNrOfValues() {
        StringTokenizer strTok = new StringTokenizer(getRegister(),".");
        return strTok.countTokens();
    }
    
    /*
     *   If the register is a sequence of numbers, separated by '.', this method
     *   retrieves the individual values.
     *   @return 
     */
    public String getValue(int index) {
        StringTokenizer strTok = new StringTokenizer(getRegister(),".");
        int count=0;
        while (strTok.hasMoreTokens()) {
            String value=strTok.nextToken();
            if (index == count++)
                return value;
            
        }
        return null;
    }
    
    public boolean isCumul() {
        return cumul;
    }
    
    public BigDecimal getWrapAroundValue() {
        return wrapAroundValue;   
    }
    
    /** Convert this ProtocolChannel to a ChannelInfo.
     * 
     * (*) If a channel has exactly 2 values they are interpreted as the C.D 
     * fields of an obiscode.  This obiscode is then used to deduce the unit of 
     * the channel.  
     * (*) Contingent cumulative flag and wraparound value are taken into
     * account.  
     * (*) Multipliers are not supported by ProtoocolChannelMap. (!!)
     * 
     * @param id 
     * @return ChannelInfo object
     */
    public ChannelInfo toChannelInfo(int id) {
        
        ChannelInfo result = null;
        
        /* if there are _exactly_ 2 values, interprete as obis C.D field */ 
        if( getNrOfValues() == 2 ) {
    
            String code = "1.1." + getRegister() + ".0.255";
            ObisCode oc = ObisCode.fromString(code);
            String name = "channel " + id + " " + code;
            Unit unit = oc.getUnitElectricity(0);
            result = new ChannelInfo(id, name, unit );
            
            if( isCumul() ) 
                result.setCumulativeWrapValue(getWrapAroundValue());
    
        } else if( getValue() != 0 ) {
            
            result = new ChannelInfo(id, "channel " + id + " " , null );
            
        }
        
        return result;
        
    }
    
}
