/*
 * ExtendedRegister.java
 *
 * Created on 19 augustus 2004, 8:53
 */

package com.energyict.dlms.cosem;
import java.io.*;
import java.util.*;

import com.energyict.protocolimpl.dlms.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;

/**
 *
 * @author  Koen
 */
public class ExtendedRegister extends Register implements CosemObject {
    public final int DEBUG=0;
    static public final int CLASSID=4;
    
    Date captureTime;
    boolean captureTimeCached=false;
    
    /** Creates a new instance of ExtendedRegister */
    public ExtendedRegister(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }
    
    
    public String toString() {
        try {
           return super.toString()+", captureTime="+getCaptureTime();
        }
        catch(IOException e) {
           return "ExtendedRegister, toString, retrieving error!";
        }
    }
    
    /**
     * Getter for property captureTime.
     * @return Value of property captureTime.
     */
    public java.util.Date getCaptureTime() throws IOException {
        if (captureTimeCached)
            return captureTime;
        else
            return (getCaptureTime(getResponseData(EXTENDED_REGISTER_CAPTURE_TIME)));
    }
    
    public java.util.Date getCaptureTime(byte[] responseData) throws IOException {
        Clock clock = new Clock(protocolLink,getObjectReference(CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
        captureTime = clock.getDateTime(responseData);
        return captureTime;
    }
    
    
    public void setCaptureTime(Date captureTime) throws IOException {
        this.captureTime=captureTime;
        captureTimeCached=true;    
    }
    
    public void setCaptureTime(OctetString octetString) throws IOException {
        Clock clock = new Clock(protocolLink,getObjectReference(CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
        clock.setDateTime(octetString);
        captureTime = clock.getDateTime();
        captureTimeCached=true;        
    }
    
    public Date getBillingDate() {
        Date retValue;
       
        retValue = super.getBillingDate();
        return retValue;
    }
    
    public int getResetCounter() {
        int retValue;
        
        retValue = super.getResetCounter();
        return retValue;
    }
    protected int getClassId() {
        return CLASSID;
    }    
    
    public String getText() throws IOException {
        try {
            DataContainer dc = new DataContainer();
            dc.parseObjectList(getResponseData(EXTENDED_REGISTER_STATUS),protocolLink.getLogger());
            return dc.getText(",");
        }
        catch(IOException e) {
            if (e.toString().indexOf("R/W denied")>=0)
                return null; //"This extended register status field is R/W denied";
            throw e;
        }
    }
    
}
