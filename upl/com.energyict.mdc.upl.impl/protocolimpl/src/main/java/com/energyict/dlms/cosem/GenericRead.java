/*
 * GenericRead.java
 *
 * Created on 30 augustus 2004, 14:27
 */

package com.energyict.dlms.cosem;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;

import com.energyict.protocolimpl.dlms.*;
import com.energyict.protocol.*;
import com.energyict.cbo.Unit;
import com.energyict.cbo.Quantity;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;

/**
 *
 * @author  Koen
 */
public class GenericRead extends AbstractCosemObject {
    public final int DEBUG=0;
    
    int attr;
    
    /** Creates a new instance of GenericRead */
    public GenericRead(ProtocolLink protocolLink, ObjectReference objectReference, int attr) {
        super(protocolLink,objectReference);
        this.attr=attr;
    }
    
    public String toString() {
        try {
           return "value="+getDataContainer().toString();
        }
        catch(IOException e) {
           return "DataGeneric retrieving error!";
        }
    }
   
    public long getValue() throws IOException {
        DataContainer dataContainer=getDataContainer();
        if (dataContainer.getRoot().isInteger(0)) {
           return (long)((Integer)dataContainer.getRoot().getElement(0)).intValue(); 
        }
        throw new IOException("DataGeneric, getValue(), invalid data value type...");
    }
    
    public String getString() throws IOException {
        DataContainer dataContainer=getDataContainer();;
        if (dataContainer.getRoot().isOctetString(0)) {
           return ((OctetString)dataContainer.getRoot().getElement(0)).toString().trim();
        }
        else if (dataContainer.getRoot().isString(0)) {
           return ((String)dataContainer.getRoot().getElement(0)).trim();
        }
        throw new IOException("DataGeneric, getString(), invalid data value type...");
    }

    public DataContainer getDataContainer() throws IOException {
        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(getResponseData(attr),protocolLink.getLogger());
        if (DEBUG >= 1) dataContainer.printDataContainer();
        return dataContainer;
    }
    
    public byte[] getResponseData() throws IOException {
        return getResponseData(attr);
    }
    
    protected int getClassId() {
        return getObjectReference().getClassId();
    }
    
}
