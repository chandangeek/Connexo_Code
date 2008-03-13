/*
 * Version.java
 *
 * Created on 17 mei 2005, 16:05
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
public class Version extends AbstractCommand {
    
    private static final int DEBUG=0;
    private static final String COMMAND="RV";
    
    String version;
    
    /** Creates a new instance of Version */
    public Version(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }
    
    public String toString() {
        return "Version: "+getVersion();
    }
    
    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) throws NestedIOException {
        
        List values=null;
        int baseVal;
        
        if (DEBUG>=1) 
           System.out.println(new String(data)); 
        String dataStr = new String(data);
        setVersion(dataStr.trim().replaceAll("\r\n",", "));
    }
    
    /**
     * Getter for property version.
     * @return Value of property version.
     */
    public java.lang.String getVersion() {
        return version;
    }
    
    /**
     * Setter for property version.
     * @param version New value of property version.
     */
    public void setVersion(java.lang.String version) {
        this.version = version;
    }
    
}
