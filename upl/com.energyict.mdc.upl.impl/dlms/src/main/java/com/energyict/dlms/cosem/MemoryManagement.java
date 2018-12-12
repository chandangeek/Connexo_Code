package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.attributes.MemoryManagementAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Models the custom class MemoryManagement, used in Beacon3100
 */
public class MemoryManagement extends AbstractCosemObject {

    private static final ObisCode DEFAULT_OBIS_CODE_LEGACY = ObisCode.fromString("0.0.128.0.20.255");
    private static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.194.96.128.0.255");


    public final static ObisCode getDefaultObisCode() {
        return DEFAULT_OBIS_CODE;
    }

    public final static ObisCode getLegacyObisCode() {
        return DEFAULT_OBIS_CODE_LEGACY;
    }

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public MemoryManagement(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.MEMORY_MANAGEMENT.getClassId();
    }

    public Structure readMemoryStatistics() throws IOException {
        return new Structure(getResponseData(MemoryManagementAttributes.MEMORY_STATISTICS), 0, 0);
    }

    public Array readFlashDevices() throws IOException {
        return new Array(getResponseData(MemoryManagementAttributes.FLASH_DEVICES),0,0);
    }
}
