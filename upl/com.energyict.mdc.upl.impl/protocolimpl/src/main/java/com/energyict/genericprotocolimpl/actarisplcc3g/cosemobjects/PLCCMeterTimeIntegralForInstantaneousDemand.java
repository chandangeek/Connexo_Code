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

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.ScalerUnit;

/**
 *
 * @author kvds
 */
public class PLCCMeterTimeIntegralForInstantaneousDemand extends AbstractPLCCObject {
    
    private ScalerUnit scalerUnit = new ScalerUnit(0, Unit.get("s"));
    private BigDecimal value=BigDecimal.valueOf(600);    
    
    /** Creates a new instance of PLCCTemplateObject */
    public PLCCMeterTimeIntegralForInstantaneousDemand(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("1.0.0.8.2.255"), DLMSClassId.REGISTER.getClassId());
    }
    
    protected void doInvoke() throws IOException {
        Register register = getCosemObjectFactory().getRegister(getId().getObisCode());
        value = register.getValueAttr().toBigDecimal();
        setScalerUnit(register.getScalerUnit());
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterTimeIntegralForInstantaneousDemand:\n");
        strBuff.append("   value="+getValue()+"\n");
        strBuff.append("   scalerUnit="+getScalerUnit()+"\n");
        return strBuff.toString();
    } 


    public BigDecimal getValue() {
        return value;
    }
    
    public ScalerUnit getScalerUnit() {
        return scalerUnit;
    }

    private void setScalerUnit(ScalerUnit scalerUnit) {
        this.scalerUnit = scalerUnit;
    }
}
