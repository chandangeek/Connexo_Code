/*
 * AllEnergy.java
 *
 * Created on 17 mei 2005, 10:52
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
 * @author  Koen
 */
public class AllEnergy extends AbstractCommand {
    
    private static final int DEBUG=0;
    private static final String COMMAND="RKA8";
    private static final int NR_OF_CHANNELS=8;
    private static final int NR_OF_TARIFFS=8;
    
    
    Quantity[][] quantities = new Quantity[NR_OF_CHANNELS][NR_OF_TARIFFS];
    
    /** Creates a new instance of AllEnergy */
    public AllEnergy(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();    
        strBuff.append("AllEnergy:\n");
        for (int tariff = 0; tariff < NR_OF_TARIFFS; tariff++) {
           strBuff.append("tariff "+tariff+": ");
           for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
               strBuff.append("ch "+channel+": "+getQuantity(channel, tariff)+", ");
           }
           strBuff.append("\n");
        }
        return strBuff.toString();
    }
    
    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }    

    protected void parse(byte[] data) throws ConnectionException, IOException {
        if (DEBUG>=1) 
           System.out.println(new String(data)); 
        
        CommandParser cp = new CommandParser(data);
        for (int tariff = 0; tariff < NR_OF_TARIFFS; tariff++) {
           List valuesL = cp.getValues("KWHL-"+(tariff+1));
           List valuesM = cp.getValues("KWHM-"+(tariff+1));
           for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
              int valueL = Integer.parseInt((String)valuesL.get(channel),16);
              int valueM = Integer.parseInt((String)valuesM.get(channel),16);
              long value = valueL+valueM*0x10000; 
              BigDecimal bd = ez7CommandFactory.getMeterInformation().calculateValue(channel, value);
              quantities[channel][tariff] = new Quantity(bd,ez7CommandFactory.getMeterInformation().getUnit(channel, true));
           }
        }
    }
    
    public Quantity getQuantity(int channel, int tariff) {
        try {
           return quantities[channel][tariff];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
