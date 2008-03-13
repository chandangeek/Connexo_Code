/*
 * AssociationSN.java
 *
 * Created on 20 augustus 2004, 16:47
 */

package com.energyict.dlms.cosem;

import java.io.*;
import java.util.*;

import com.energyict.protocolimpl.dlms.*;
import com.energyict.protocol.*;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
/**
 *
 * @author  Koen
 */
public class AssociationSN extends AbstractCosemObject {
    public final int DEBUG=0;
    static public final int CLASSID=12;
    
    UniversalObject[] buffer;
    
    /** Creates a new instance of AssociationSN */
    public AssociationSN(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference );
    }
    
    public UniversalObject[] getBuffer() throws IOException {
        buffer = data2UOL(getResponseData(ASSOC_SN_ATTR_OBJ_LST));
        return buffer;
    }
    
    protected int getClassId() {
        return CLASSID;
    }
    
}
