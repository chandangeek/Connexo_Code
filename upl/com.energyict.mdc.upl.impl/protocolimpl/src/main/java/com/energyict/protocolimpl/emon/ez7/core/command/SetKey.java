/*
 * SetKey.java
 *
 * Created on 23 mei 2005, 14:59
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
public class SetKey extends AbstractCommand {
    
    private static final int DEBUG=0;
    private static final String COMMAND="SK";
    
    static public final String[] ACCESSLEVELS={"Loged OFF","Read only","Read/Bill","Unknown access level","Unrestricted"};
    
    /** Creates a new instance of SetKey */
    public SetKey(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }
    
    
    public void logon(String password) throws ConnectionException, IOException {
        
        // temporary!
        // apply encoding algorithm if we have it...
        if (ez7CommandFactory.getVerifyKey().useEncoding())
            throw new IOException("SetKey, logon(), Encoding algorithm required!");
        
        
        StringBuffer strBuff = new StringBuffer();
        byte[] data = password.toUpperCase().getBytes();
        for (int i=0;i<8;i++) {
            byte[] pw = new byte[2];
            for (int t=0;t<2;t++) {
                pw[t]=data[i*2+t];
            }
            strBuff.append(new String(pw)+" ");
        }
        ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND,strBuff.toString());
    }
    
    public void logoff() throws ConnectionException, IOException {
        ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
    }
    
    
    public int getAccessLevel() throws ConnectionException, IOException {
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND+"?");
        String accessLevel = new String(data);
        accessLevel = accessLevel.trim();
        return Integer.parseInt(accessLevel);
    }
    
    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        //byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        //parse(data);
        
    }
    
}
