package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

import java.io.IOException;

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
        return new ObjectIdentification(ObisCode.fromString("0.0.16.0.1.255"), DLMSClassId.REGISTER_MONITOR.getClassId());
    }
    protected void doInvoke() throws IOException {
        registerMonitor = getCosemObjectFactory().getRegisterMonitor(getId().getObisCode());
    }

    public com.energyict.protocolimpl.edf.messages.objects.DemandManagement readDemandManagement() throws IOException {
        com.energyict.protocolimpl.edf.messages.objects.DemandManagement demandManagement = new com.energyict.protocolimpl.edf.messages.objects.DemandManagement();
        demandManagement.setMaxloadThreshold(registerMonitor.readThresholds().getDataType(0).intValue());
        demandManagement.setSubscribedThreshold(registerMonitor.readThresholds().getDataType(1).intValue());
        return demandManagement;
    }

    public void writeDemandManagement(com.energyict.protocolimpl.edf.messages.objects.DemandManagement demandManagement) throws IOException {
        Array array = new Array();
        array.addDataType(new Unsigned16(demandManagement.getMaxloadThreshold()));
        array.addDataType(new Unsigned16(demandManagement.getSubscribedThreshold()));
        registerMonitor.writeThresholds(array);
    }

}
