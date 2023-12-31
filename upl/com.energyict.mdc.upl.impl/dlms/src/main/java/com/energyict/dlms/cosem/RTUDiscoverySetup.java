package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.attributes.RTUDiscoveryAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Created by cisac on 11/21/2016.
 */
public class RTUDiscoverySetup extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.18.128.0.0.255");

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public RTUDiscoverySetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.RTU_DISCOVERY_SETUP.getClassId();
    }

    public void enableInterfaces(Array interfacesArray) throws IOException {
        write(RTUDiscoveryAttributes.ENABLED_INTERFACES, interfacesArray.getBEREncodedByteArray());
    }
}
