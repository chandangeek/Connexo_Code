

package com.energyict.dlms.cosem;

import java.io.*;
import java.util.*;

import com.energyict.cbo.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.ProtocolLink;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.AbstractCosemObject;
/**
 *
 * @author  Koen
 */
public class ScriptTable extends AbstractCosemObject {
    
    private Array scripts=null;
    
    /** Creates a new instance of Data */
    public ScriptTable(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }
    
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_SCRIPTTABLE;
    }
    
    public void writeScripts(Array scripts) throws IOException {
        write(2, scripts.getBEREncodedByteArray());
    }
    public Array readScripts() throws IOException {
        if (scripts == null) {
            scripts = (Array)AXDRDecoder.decode(getLNResponseData(2));
        }
        return scripts;
    }    
    
    public void execute(int data) throws IOException {
        Unsigned16 u16 = new Unsigned16(data);
        invoke(1,u16.getBEREncodedByteArray());
    }
    
}
