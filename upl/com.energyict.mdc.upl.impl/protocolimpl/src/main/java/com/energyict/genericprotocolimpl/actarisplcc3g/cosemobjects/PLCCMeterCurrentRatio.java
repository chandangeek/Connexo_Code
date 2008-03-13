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

public class PLCCMeterCurrentRatio extends AbstractPLCCObject {
    
    Data data=null;
    
    // multiplier
    // 1, 10, 100, 500, 1000
    BigDecimal energyMultiplier=null;
    
    public PLCCMeterCurrentRatio(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification("1.1.0.4.2.255", AbstractCosemObject.CLASSID_DATA );
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterCurrentRatio:\n");
        try {
            strBuff.append("   energyMultiplier="+getEnergyMultiplier()+"\n");
        }
        catch (IOException e) {
            strBuff.append(e.toString());
        }
        return strBuff.toString();
    }    
    
    
    protected void doInvoke() throws IOException {
        data = getCosemObjectFactory().getData(getId().getObisCode());
    }

    public void writeEnergyMultiplier(BigDecimal energyMultiplier) throws IOException {
        Unsigned16 val = new Unsigned16(energyMultiplier.intValue());
        data.setValueAttr(val);
        this.energyMultiplier=energyMultiplier;
    }
    
    public BigDecimal getEnergyMultiplier() throws IOException {
        if (energyMultiplier==null) {
            energyMultiplier = readEnergyMultiplier();
        }
        return energyMultiplier;
    }
    
    public BigDecimal readEnergyMultiplier() throws IOException {
        return data.getValueAttr().toBigDecimal();
    }
}