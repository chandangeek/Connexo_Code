/*
 * Class15EventLogConfiguration.java
 *
 * Created on 25 juli 2005, 11:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.elster.alpha.core.connection.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.cbo.*;

/**
 *
 * @author koen
 */
public class Class15EventLogConfiguration extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(15,15,true);
           
    
    int EVSIZE;
    int EVSEL1;
        
    /** Creates a new instance of Class15EventLogConfiguration */
    public Class15EventLogConfiguration(ClassFactory classFactory) {
        super(classFactory);
    }
    
    public String toString() {
        return "Class15EventLogConfiguration: EVSIZE="+EVSIZE+", EVSEL1=0x"+Integer.toHexString(EVSEL1);
    }
    
    protected void parse(byte[] data) throws IOException {
        EVSIZE = ProtocolUtils.getInt(data,0,2);
        EVSEL1 = ProtocolUtils.getInt(data,2,1);
    }
    
    
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification; 
    }    

    public int getEVSIZE() {
        return EVSIZE;
    }

    public int getEVSEL1() {
        return EVSEL1;
    }
    
}
