/*
 * IMONInformation.java
 *
 * Created on 4 juli 2005, 16:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.emon.ez7.core.command;  

import java.io.*;
import java.util.*;
import java.text.*;
import java.math.BigDecimal;

import com.energyict.cbo.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.emon.ez7.core.*;
import com.energyict.dialer.connection.ConnectionException;

/**
 *
 * @author Koen
 */
public class IMONInformation extends AbstractCommand {
    
    private static final int DEBUG=0;
    private static final String COMMAND="VI";
    
    private TimeZone timeZone;
    private boolean useDST;
    
    /** Creates a new instance of IMONInformation */
    public IMONInformation(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }
    
    public String toString() {
        return "IMONInformation: useDST="+isUseDST()+", timeZone="+getTimeZone();
    }
    
    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
        verifyTimeZone();
    }    
    
    private void verifyTimeZone() throws IOException {
        if (ez7CommandFactory.getEz7().getTimeZone().getRawOffset() != getTimeZone().getRawOffset())
            throw new IOException("Meter timezone ("+getTimeZone().getDisplayName()+") raw offset differs from the configured device timezone ("+ez7CommandFactory.getEz7().getTimeZone().getDisplayName()+") raw offset! correct first!");
        if (ez7CommandFactory.getEz7().getTimeZone().useDaylightTime() != isUseDST())
            throw new IOException("Meter timezone ("+getTimeZone().getDisplayName()+") use DST ("+useDST+") differs from the configured device timezone ("+ez7CommandFactory.getEz7().getTimeZone().getDisplayName()+") use DST ("+ez7CommandFactory.getEz7().getTimeZone().useDaylightTime()+")! correct first!");            
    }
    
    protected void parse(byte[] data) {
        if (DEBUG>=1) 
           System.out.println(new String(data)); 
        int value;
        CommandParser cp = new CommandParser(data);
        
        List values = cp.getValues("LINE-1");
        value = Integer.parseInt((String)values.get(1),16);
        timeZone = (buildTimeZone(value&0x001F));
        useDST = ((value&0X0020)==0);
    }
    
    private TimeZone buildTimeZone(int val) {
        int offset = val-12;
        String ID = "GMT"+(offset>=0?("+"+offset):(""+offset));
        return TimeZone.getTimeZone(ID);
    }
    
    public static void main(String[] args) {
        IMONInformation imonInformation = new IMONInformation(null);
        TimeZone tz = imonInformation.buildTimeZone(7);
        System.out.println(tz);
        System.out.println(tz.getDisplayName(false,TimeZone.SHORT));
        
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isUseDST() {
        return useDST;
    }
    
}
