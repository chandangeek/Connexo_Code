
package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;



import com.energyict.cbo.*;
import com.energyict.protocolimpl.dlms.*;
import java.io.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.*;

import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.ScalerUnit;

/**
 *
 * @author kvds
 */
public class PLCCMeterInstantaneousDemand extends AbstractPLCCObject {
    
    private ScalerUnit scalerUnit = new ScalerUnit(0, Unit.get("W"));
    private long value;    
    
    /** Creates a new instance of PLCCMeterInstantaneousDemand */
    public PLCCMeterInstantaneousDemand(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("1.0.1.7.0.255"), DLMSClassId.REGISTER.getClassId());
    }
    
    protected void doInvoke() throws IOException {
        Register register = getCosemObjectFactory().getRegister(getId().getObisCode());
        setValue(register.getValueAttr().longValue());
//        setScalerUnit(register.getScalerUnit());
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterInstantaneousDemand:\n");
        strBuff.append("   value="+getValue()+"\n");
        strBuff.append("   scalerUnit="+getScalerUnit()+"\n");
        return strBuff.toString();
    } 


    public long getValue() {
        return value;
    }

    private void setValue(long value) {
        this.value = value;
    }

    public ScalerUnit getScalerUnit() {
        return scalerUnit;
    }

    private void setScalerUnit(ScalerUnit scalerUnit) {
        this.scalerUnit = scalerUnit;
    }
}
