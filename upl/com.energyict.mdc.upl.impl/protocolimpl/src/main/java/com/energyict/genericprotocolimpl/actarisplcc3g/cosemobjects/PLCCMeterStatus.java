package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ObjectIdentification;

public class PLCCMeterStatus extends AbstractPLCCObject {


    BigDecimal status=null;

    public PLCCMeterStatus(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification("0.0.96.5.0.255", DLMSClassId.DATA.getClassId() );
    }

    protected void doInvoke() throws IOException {
        status = AXDRDecoder.decode(getCosemObjectFactory().getData(getId().getObisCode()).getData()).toBigDecimal();
    }

    public BigDecimal getStatus() throws IOException {
        return status;
    }
}