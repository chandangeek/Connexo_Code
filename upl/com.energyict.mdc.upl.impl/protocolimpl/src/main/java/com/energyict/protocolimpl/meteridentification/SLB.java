/*
 * SLB.java
 *
 * Created on 15 april 2005, 11:53
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;


/**
 *
 * @author  Koen
 */
public class SLB extends AbstractManufacturer {
    
    
    
    /** Creates a new instance of SLB */
    public SLB() {
    }
    
    public String getManufacturer() throws IOException {
        return "Schlumberger";
    }    
    
    public String getMeterProtocolClass() throws IOException {
        if ((getSignOnString()==null) || (getSignOnString().indexOf("MINICOR") >= 0)) {
           return "com.energyict.protocolimpl.actarissevc.SEVC";
        } else {
//            return "com.energyict.protocolimpl.dlms.DLMSLNSL7000";
            throw new IOException("Unknown metertype");
        }
    }    
    
    public String[] getMeterSerialNumberRegisters() throws IOException {
        if ((getSignOnString()==null) || (getSignOnString().indexOf("MINICOR") >= 0))
           return null;
        else
           return new String[]{"C.70.1"};
    }    
    
    public String getMeterDescription() throws IOException {
        if ((getSignOnString()==null) || (getSignOnString().indexOf("MINICOR") >= 0))
           return "SEVC VHI gascorrector";
        else
           return "DLMS-LN Actaris SL7000";
    }
    
}
