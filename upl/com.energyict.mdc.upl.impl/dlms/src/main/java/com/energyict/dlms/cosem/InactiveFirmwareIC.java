package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * INACTIVE FIRMWARE IC class id 20027
 */
public class InactiveFirmwareIC extends AbstractCosemObject {
    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.128.96.132.0.255");
    public static final int COPY_ACTIVE_FIRMWARE_TO_INACTIVE_FIRMWARE_METHOD_ID = 1;
    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public InactiveFirmwareIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.INACTIVE_FIRMWARE_IC.getClassId();
    }

    public void copyActiveFirmwareToInactiveFirmware() throws IOException {
        if (getObjectReference().isLNReference()) {
            invoke(COPY_ACTIVE_FIRMWARE_TO_INACTIVE_FIRMWARE_METHOD_ID);
        }
    }
}
