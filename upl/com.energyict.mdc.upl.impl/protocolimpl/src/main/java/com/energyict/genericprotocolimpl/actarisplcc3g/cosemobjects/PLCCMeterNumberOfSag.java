/*
 * PLCCTemplateObject.java
 *
 * Created on 3 december 2007, 13:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

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
public class PLCCMeterNumberOfSag extends AbstractPLCCObject {
    
    private BigDecimal value=null;
    
    /** Creates a new instance of PLCCMeterNumberOfSag */
    public PLCCMeterNumberOfSag(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("1.0.12.32.0.255"), AbstractCosemObject.CLASSID_DATA);
    }
    
    protected void doInvoke() throws IOException {
        Data data = getCosemObjectFactory().getData(getId().getObisCode());
        value = data.getValueAttr().toBigDecimal();
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterNumberOfSag:\n");
        strBuff.append("   value="+getValue()+"\n");
        return strBuff.toString();
    } 

    public BigDecimal getValue() {
        return value;
    }

}
