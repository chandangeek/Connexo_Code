package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ObjectIdentification;

public class PLCCMeterCurrentRatio extends AbstractPLCCObject {

    Data data=null;

    // multiplier
    // 1, 10, 100, 500, 1000
    BigDecimal energyMultiplier=null;

    public PLCCMeterCurrentRatio(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification("1.1.0.4.2.255", DLMSClassId.DATA.getClassId() );
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