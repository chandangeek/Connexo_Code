/*
 * VerifyKey.java
 *
 * Created on 23 mei 2005, 14:45
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import java.io.*;
import java.util.*;
import java.text.*;

import com.energyict.cbo.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.emon.ez7.core.*;
import com.energyict.dialer.connection.ConnectionException;

/**
 *
 * @author  Koen
 */
public class VerifyKey extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String COMMAND="VK";
    private static final int NR_OF_VALUES = 8;
    
    int[] values = new int[NR_OF_VALUES];
    
    /** Creates a new instance of VerifyKey */
    public VerifyKey(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("VerifyKey:\n");
        for (int value=0;value<NR_OF_VALUES;value++) {
            strBuff.append("0x"+Integer.toHexString(values[value])+" ");
        }
        return strBuff.toString();
    }

    public boolean useEncoding() {
        return (values[0]/0x100)!=0;
    }
    
    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
        
    }

    private void parse(byte[] data) {
        if (DEBUG>=1) 
           System.out.println(new String(data)); 
        CommandParser cp = new CommandParser(data); 
        List vals = cp.getValues("LINE-1");
        for (int value=0;value<NR_OF_VALUES;value++) {
            values[value] = Integer.parseInt((String)vals.get(value),16);
        }
        
    }
    
}

