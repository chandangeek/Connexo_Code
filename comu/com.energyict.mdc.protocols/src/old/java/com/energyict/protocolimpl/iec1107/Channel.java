/*
 * Channel.java
 *
 * Created on 12 september 2003, 9:53
 */

package com.energyict.protocolimpl.iec1107;

import com.energyict.mdc.protocol.api.InvalidPropertyException;

import java.math.BigDecimal;
/**
 *
 * @author  Koen
 * A channel has a (register) code.
 * E.g. 1.0.3+7 where
 *      1.0.3 is the regisqter code
 *      +7 tells the channel is cumulative and has a 7 digit wrap around value 0000000 -> 9999999 decimal!
 */
public class Channel {

    String register=null;
    boolean cumul=false;
    BigDecimal wrapAroundValue=null;

    /** Creates a new instance of Channel */
    public Channel(String strChannel) throws InvalidPropertyException {
        int index=0;
        if ((index=strChannel.indexOf("+")) != -1) {
            cumul = true;
            register = strChannel.substring(0,strChannel.indexOf("+"));
            if (index == (strChannel.length()-1)) throw new InvalidPropertyException("Error in ChannelMap property! Nr of digits must be set for a cumul channel. Register must be followed by '+' sign followed by nr of digits (e.g. 1.0.3+7)");
            int digits = Integer.parseInt(strChannel.substring(index+1,strChannel.length()));
            wrapAroundValue = new BigDecimal(Math.pow(10, digits));
        }
        else
            register = strChannel;
    }

    public String getRegister() {
        return register;
    }

    public boolean isCumul() {
        return cumul;
    }

    public BigDecimal getWrapAroundValue() {
        return wrapAroundValue;
    }


}
