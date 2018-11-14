package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.attributes.FirmwareConfigurationAttributes;
import com.energyict.dlms.cosem.methods.FirmwareConfigurationMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * class id = 20024, version = 0, logical name = 0-128:96.130.0.255 (0080608200FF)
 * This manufacturer-defined COSEM IC allows for changing the active firmware configuration.
 * This includes resetting the data partition.
 */
public class FirmwareConfigurationIC extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.128.96.130.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public FirmwareConfigurationIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.FIRMWARE_CONFIGURATION_IC.getClassId();
    }

    public AbstractDataType readAttribute(FirmwareConfigurationAttributes attribute, AbstractDataType abstractDataType) throws IOException {
        return readDataType(attribute, abstractDataType.getClass());
    }

    public void writeFirmwareConfigurationAttribute(FirmwareConfigurationAttributes attribute, AbstractDataType data) throws IOException {
        write(attribute, data);
    }

    public void invokeFirmwareConfigurationMethod(FirmwareConfigurationMethods firmwareConfigurationMethod, AbstractDataType data) throws IOException {
        methodInvoke(firmwareConfigurationMethod, data);
    }

}