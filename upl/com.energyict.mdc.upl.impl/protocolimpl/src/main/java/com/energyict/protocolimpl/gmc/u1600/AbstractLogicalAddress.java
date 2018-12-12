/*
 * AbstractLogicalAddress.java
 *
 * Created on 7 juli 2004, 11:03
 */

package com.energyict.protocolimpl.gmc.u1600;

import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.IOException;
import java.util.TimeZone;

//import com.energyict.protocolimpl.myprotocol.*;
/**
 *
 * @author  Koen
 */
abstract public class AbstractLogicalAddress {
    
    abstract public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException;
    
    int channel;
    
    LogicalAddressFactory logicalAddressFactory;
    
    /** Creates a new instance of AbstractLogicalAddress */
    public AbstractLogicalAddress(int channel,int size,LogicalAddressFactory logicalAddressFactory) {
        this.channel=channel;
        this.logicalAddressFactory=logicalAddressFactory;
    }
    
    /*public void retrieve() throws IOException {
        byte[] data = getLogicalAddress();
        parse(data,getLogicalAddressFactory().getU1600().getTimeZone());
    }*/
    
    /*
     *  Must be overridden to implement the data builder...
     */
    protected byte[] buildData() {
        return null;
    }
    
   
     
     
      public void retrieve() throws IOException {
        byte[] data = getILONRegister();
        parse(data,getLogicalAddressFactory().getU1600().getTimeZone());
    }
    
    /* public void retrieve() throws IOException {
        byte[] data = getLogicalAddress();
        parse(data,getLogicalAddressFactory().getU1600().getTimeZone());
    }*/
    
        
    
    
    private byte[] getILONRegister() throws IOException {
        //StringBuffer strbuff = new StringBuffer();
        getLogicalAddressFactory().getU1600().getEclConnection().sendLONCommandFrame(getChannel());
        byte[] ba = getLogicalAddressFactory().getU1600().getEclConnection().receiveLONFrame();
        return  validateData(ba);
    }
    
      private byte[] validateData(byte[] data) throws ProtocolConnectionException {
        String str = new String(data);
        String strValue = "";
        int iStart = str.indexOf("=");
        int iStop = str.indexOf(0x0D,iStart);
        if ((iStart > 0) & (iStop > 0))
        {
            strValue = str.substring(iStart+1,iStop);
            // strValue.trim(); KV 22072005 unused code!! returns trimmed string!! What did Weinert means by this?
        }
        else
             throw new ProtocolConnectionException("AbstractLogicalAddress, validateData, "+getLogicalAddressFactory().getMeterExceptionInfo().getExceptionInfo(str));

       
        // We know about ERRDAT and ERRADD as returned error codes from the Indigo+ meter.
        // Probably there are more...
        if (str.  indexOf("Fehler") != -1) {
            throw new ProtocolConnectionException("AbstractLogicalAddress, validateData, "+getLogicalAddressFactory().getMeterExceptionInfo().getExceptionInfo(str));
        }
        return strValue.getBytes();
    }
    
    
    protected String buildLength(int value,int length) {
        String str=Integer.toHexString(value);
        StringBuffer strbuff = new StringBuffer();
        if (length >= str.length())
            for (int i=0;i<(length-str.length());i++)
                strbuff.append('0');
        strbuff.append(str);
        return strbuff.toString();
    }
        
   
    
    /**
     * Getter for property logicalAddressFactory.
     * @return Value of property logicalAddressFactory.
     */
    public com.energyict.protocolimpl.gmc.u1600.LogicalAddressFactory getLogicalAddressFactory() {
        return logicalAddressFactory;
    }
    
    /**
     * Setter for property logicalAddressFactory.
     * @param logicalAddressFactory New value of property logicalAddressFactory.
     */
    public void setLogicalAddressFactory(com.energyict.protocolimpl.gmc.u1600.LogicalAddressFactory logicalAddressFactory) {
        this.logicalAddressFactory = logicalAddressFactory;
    }    
    
    /**
     * Getter for property size.
     * @return Value of property size.
     */
    public int getChannel() {
        return channel;
    }
    
   
    

   
}
