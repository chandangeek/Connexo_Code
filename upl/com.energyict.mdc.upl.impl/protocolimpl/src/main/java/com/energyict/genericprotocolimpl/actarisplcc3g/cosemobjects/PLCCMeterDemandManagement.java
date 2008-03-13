package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.RegisterMonitor;

/**
 *
 * @author kvds
 */
public class PLCCMeterDemandManagement extends AbstractPLCCObject {
    
    RegisterMonitor registerMonitor=null;
    
    /** Creates a new instance of PLCCMeterMovingPeak */
    public PLCCMeterDemandManagement(PLCCObjectFactory objectFactory) { 
        super(objectFactory);
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("0.0.16.0.1.255"), AbstractCosemObject.CLASSID_REGISTER_MONITOR);
    }
    protected void doInvoke() throws IOException {
        registerMonitor = getCosemObjectFactory().getRegisterMonitor(getId().getObisCode());
    }
    
    public com.energyict.edf.messages.objects.DemandManagement readDemandManagement() throws IOException {
        com.energyict.edf.messages.objects.DemandManagement demandManagement = new com.energyict.edf.messages.objects.DemandManagement();
        demandManagement.setMaxloadThreshold(registerMonitor.readThresholds().getDataType(0).intValue());
        demandManagement.setSubscribedThreshold(registerMonitor.readThresholds().getDataType(1).intValue());
        return demandManagement;
    }    
    
    public void writeDemandManagement(com.energyict.edf.messages.objects.DemandManagement demandManagement) throws IOException {
        Array array = new Array();
        array.addDataType(new Unsigned16(demandManagement.getMaxloadThreshold()));
        array.addDataType(new Unsigned16(demandManagement.getSubscribedThreshold()));
        registerMonitor.writeThresholds(array);
    }
    
}
