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
public class PLCCMeterNumberOfShortPowerFailures extends AbstractPLCCObject {

    BigDecimal value = null;

    /** Creates a new instance of PLCCMeterNumberOfShortPowerFailures */
    public PLCCMeterNumberOfShortPowerFailures(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("0.0.96.7.0.255"), DLMSClassId.DATA.getClassId());
    }

    protected void doInvoke() throws IOException {
        Data data = getCosemObjectFactory().getData(getId().getObisCode());
        value = data.getValueAttr().toBigDecimal();
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterNumberOfShortPowerFailures:\n");
        strBuff.append("   value="+getValue()+"\n");
        return strBuff.toString();
    }


    public BigDecimal getValue() {
        return value;
    }

}
