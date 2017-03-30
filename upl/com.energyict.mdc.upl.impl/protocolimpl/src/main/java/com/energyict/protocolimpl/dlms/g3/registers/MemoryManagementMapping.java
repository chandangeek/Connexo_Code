package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.MemoryManagement;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.MemoryManagementAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Created by iulian on 3/30/2017.
 */
public class MemoryManagementMapping extends G3Mapping {
    MemoryManagementAttributesMapping memoryManagementAttributesMapping;

    protected MemoryManagementMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    //Set the F-Filed to 255
    public ObisCode getBaseObisCode() {
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 5, (byte) 255);
    }


    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    private RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (memoryManagementAttributesMapping.canRead(obisCode)){
            return memoryManagementAttributesMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (memoryManagementAttributesMapping == null){
            memoryManagementAttributesMapping = new MemoryManagementAttributesMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getF();        //The B-field of the obiscode indicates which attribute is being read
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);

        if (memoryManagementAttributesMapping.canRead(getObisCode())){
            return memoryManagementAttributesMapping.parse(getObisCode(), abstractDataType);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if (getObisCode().equalsIgnoreBillingField(MemoryManagement.getDefaultObisCode()) ||
                getObisCode().equalsIgnoreBillingField(MemoryManagement.getLegacyObisCode())){
            return DLMSClassId.MEMORY_MANAGEMENT.getClassId();
        } else {
            return -1;
        }
    }
}
