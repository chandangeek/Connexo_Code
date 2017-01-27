/*
 * Class31ModemBillingCallConfiguration.java
 *
 * Created on 11 juli 2005, 15:36
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Class31ModemBillingCallConfiguration extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(31,46,true);
    private String dialString; // 36 bytes
    //RESERVED [6]
    private int timingWindowFrom; // 1 byte
    private int timingWindowTo; // 1 byte
    private int AUTODAY; // 1 byte
    private byte[] cacheData=null;

    public String toString() {
        return "Class31ModemBillingCallConfiguration: dialString="+getDialString()+", timingWindowFrom="+getTimingWindowFrom()+", timingWindowTo="+getTimingWindowTo()+", AUTODAY="+getAUTODAY();                
    }
    
    /** Creates a new instance of Class31ModemBillingCallConfiguration */
    public Class31ModemBillingCallConfiguration(ClassFactory classFactory) {
        super(classFactory);
    }
    
    protected void parse(byte[] data) throws IOException {
        
        cacheData = ProtocolUtils.getSubArray2(data, 0, data.length-1);
        int offset = 0;
        setDialString(new String(ProtocolUtils.getSubArray(data,offset, 36)));
        offset += 36;
        offset += 6; // reserved bytes
        setTimingWindowFrom(ProtocolUtils.getBCD2Int(data, offset++,1));
        setTimingWindowTo(ProtocolUtils.getBCD2Int(data, offset++,1));
        setAUTODAY(ProtocolUtils.getInt(data, offset++, 1));
    }
    
    protected byte[] prepareWrite() throws IOException {
        
        
        // 0..35 dialstring
        if (getDialString().compareTo("remove")==0) {
            for (int i=0;i<35;i++) 
                cacheData[i]=0;                
        }
        else {
            byte[] dialStringBytes = getDialString().getBytes();
            for (int i=0;i<35;i++) {
                if (i<dialStringBytes.length)
                   cacheData[i] = dialStringBytes[i];
                else
                   cacheData[i]=0;                
            }
        }
        
        cacheData[42] = ProtocolUtils.hex2BCD(getTimingWindowFrom());
        cacheData[43] = ProtocolUtils.hex2BCD(getTimingWindowTo());
        return cacheData;
    }    
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification; 
    }

    public String getDialString() {
        return dialString;
    }

    public void setDialString(String dialString) {
        this.dialString = dialString;
    }

    public int getTimingWindowFrom() {
        return timingWindowFrom;
    }

    public void setTimingWindowFrom(int timingWindowFrom) {
        this.timingWindowFrom = timingWindowFrom;
    }

    public int getTimingWindowTo() {
        return timingWindowTo;
    }

    public void setTimingWindowTo(int timingWindowTo) {
        this.timingWindowTo = timingWindowTo;
    }

    private int getAUTODAY() {
        return AUTODAY;
    }

    private void setAUTODAY(int AUTODAY) {
        this.AUTODAY = AUTODAY;
    }


    
}
