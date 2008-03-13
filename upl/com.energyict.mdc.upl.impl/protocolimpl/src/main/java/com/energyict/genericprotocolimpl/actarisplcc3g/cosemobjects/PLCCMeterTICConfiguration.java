package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.*;
import java.math.*;
import java.util.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.*;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.Data;

public class PLCCMeterTICConfiguration extends AbstractPLCCObject {
    
    // 0=veille, 1=métrologie, 2=standard, 3=téléinfo client
    Data data=null;
    int mode;
    
    public PLCCMeterTICConfiguration(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification("0.0.96.3.2.255", AbstractCosemObject.CLASSID_DATA );
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterTICConfiguration:\n");
        try {
            strBuff.append("   mode="+readMode()+"\n");
        }
        catch (IOException e) {
            strBuff.append(e.toString());
        }
        return strBuff.toString();
    }    
    
    
    protected void doInvoke() throws IOException {
        data = getCosemObjectFactory().getData(getId().getObisCode());
    }

    public void writeMode(int mode) throws IOException {
        Unsigned8 val = new Unsigned8(mode);
        data.setValueAttr(val);
        this.mode=mode;
    }
    
    public int readMode() throws IOException {
        return data.getValueAttr().intValue();
    }
}