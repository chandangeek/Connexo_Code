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

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.obis.ObisCode;

/**
 *
 * @author kvds
 */
public class PLCCMeterErrorCodeRegister extends AbstractPLCCObject {

    BigDecimal errorCode=null;

    /** Creates a new instance of PLCCTemplateObject */
    public PLCCMeterErrorCodeRegister(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    protected void doInvoke() throws IOException {
        AbstractDataType data = getCosemObjectFactory().getData(getId().getObisCode()).getValueAttr();
        if (data.isOctetString()) {
            OctetString o = (OctetString)data;
            long val = (long)(o.getOctetStr()[0]<<24 |
                              o.getOctetStr()[1]<<16 |
                              o.getOctetStr()[2]<<8 |
                              o.getOctetStr()[3]);
            errorCode = BigDecimal.valueOf(val);
        }
        else {
            Unsigned32 o = (Unsigned32)data;
            errorCode = BigDecimal.valueOf(o.longValue());
        }
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("0.0.97.97.0.255"), DLMSClassId.DATA.getClassId());
    }

    public BigDecimal getErrorCode() {
        return errorCode;
    }
}
