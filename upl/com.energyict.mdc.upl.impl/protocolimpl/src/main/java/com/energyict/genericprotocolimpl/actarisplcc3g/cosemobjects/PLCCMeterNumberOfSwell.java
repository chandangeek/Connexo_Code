package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.cbo.*;
import com.energyict.protocolimpl.dlms.*;
import java.io.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.*;

import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.ObjectIdentification;
import java.math.*;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.Data;

/**
 *
 * @author kvds
 */
public class PLCCMeterNumberOfSwell extends AbstractPLCCObject {
    
    BigDecimal value = null;
    
    /** Creates a new instance of PLCCMeterNumberOfSwell */
    public PLCCMeterNumberOfSwell(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("1.0.12.36.0.255"), AbstractCosemObject.CLASSID_DATA);
    }
    
    protected void doInvoke() throws IOException {
        Data data = getCosemObjectFactory().getData(getId().getObisCode());
        value = data.getValueAttr().toBigDecimal();
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterNumberOfSwell:\n");
        strBuff.append("   value="+getValue()+"\n");
        return strBuff.toString();
    } 


    public BigDecimal getValue() {
        return value;
    }
}
