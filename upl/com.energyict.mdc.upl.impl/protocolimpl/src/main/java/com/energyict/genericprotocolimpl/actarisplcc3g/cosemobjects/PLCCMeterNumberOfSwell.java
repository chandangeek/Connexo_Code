package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.obis.ObisCode;

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
        return new ObjectIdentification(ObisCode.fromString("1.0.12.36.0.255"), DLMSClassId.DATA.getClassId());
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
