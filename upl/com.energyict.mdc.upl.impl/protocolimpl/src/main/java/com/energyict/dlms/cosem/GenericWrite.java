/*
 * GenericWrite.java
 *
 * Created on 30 augustus 2004, 15:21
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
import com.energyict.dlms.ProtocolLink;
/**
 *
 * @author  Koen
 */
public class GenericWrite extends AbstractCosemObject {
    public final int DEBUG=0;
    
    int attr;
    
    /** Creates a new instance of GenericRead */
    public GenericWrite(ProtocolLink protocolLink, ObjectReference objectReference, int attr) {
        super(protocolLink,objectReference);
        this.attr=attr;
    }
    
    public void write(byte[] data) throws IOException {
        write(attr,data);
    }
    
    protected int getClassId() {
        return getObjectReference().getClassId();
    }
    
}
