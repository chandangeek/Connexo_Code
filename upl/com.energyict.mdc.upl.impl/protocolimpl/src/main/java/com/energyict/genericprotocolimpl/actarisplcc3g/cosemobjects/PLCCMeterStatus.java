package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.*;
import java.math.*;
import java.util.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.*;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.dlms.cosem.AbstractCosemObject;

public class PLCCMeterStatus extends AbstractPLCCObject {
    
    
    BigDecimal status=null;
    
    public PLCCMeterStatus(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification("0.0.96.5.0.255", AbstractCosemObject.CLASSID_DATA );
    }
    
    protected void doInvoke() throws IOException {
        status = AXDRDecoder.decode(getCosemObjectFactory().getData(getId().getObisCode()).getData()).toBigDecimal();
    }
    
    public BigDecimal getStatus() throws IOException {
        return status;
    }
}