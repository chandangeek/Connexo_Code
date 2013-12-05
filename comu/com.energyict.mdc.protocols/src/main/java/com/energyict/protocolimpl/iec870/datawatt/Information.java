/*
 * Information.java
 *
 * Created on 18 juli 2003, 15:16
 */

package com.energyict.protocolimpl.iec870.datawatt;

import com.energyict.protocolimpl.iec870.CP24Time2a;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class Information {

    protected Date date=null;
    protected int status;
    protected CP24Time2a cp24;
    protected Channel channel;
    protected BigDecimal value;

    /** Creates a new instance of Information */
    public Information() {
    }
    public boolean isInvalid() {
        return cp24.isInValid();
    }

    public boolean isWithTimetag() {
        return (date != null);
    }

    public String toString() {
        if (date != null)
            return "channel="+channel.getChannelId()+" "+getDate()+" "+cp24.isInValid()+" val="+value.toString()+" status="+status+" addresstype=0x"+Integer.toHexString(channel.getChannelType());
        else
            return "channel="+channel.getChannelId()+" val="+value.toString()+" status="+status+" addresstype=0x"+Integer.toHexString(channel.getChannelType());
    }


    public Date getDate() {
        return date;
    }
    public BigDecimal getValue() {
        return value;
    }
    public int getStatus() {
        return status;
    }
    public Channel getChannel() {
        return channel;
    }

}
