package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.ConcentratorSetupAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Maps the ConcentratorSetup object attributes into field F of the obis code
 */
public class ConcentratorSetupMapping extends G3Mapping{

    protected ConcentratorSetupAttributesMapping dcAttributesMapping;


    public ConcentratorSetupMapping(ObisCode obis) {
        super(obis);
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory){
        if (dcAttributesMapping == null){
            dcAttributesMapping = new ConcentratorSetupAttributesMapping(cosemObjectFactory);
        }
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (dcAttributesMapping.canRead(obisCode)) {
            return dcAttributesMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    @Override
    public int getAttributeNumber() {
        switch (getObisCode().getF()) {        //The F-field of the obiscode indicates which attribute is being read
            case 2:
                return  2;
            case 3:
                return  3;
            case 4:
            case 41:
            case 42:
                return  4;
            case 5:
                return  5;

        }
        return getObisCode().getF();
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (dcAttributesMapping.canRead(getObisCode())) {
            return dcAttributesMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    @Override
    public ObisCode getBaseObisCode() {                 //Set the F-field to 255
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 5, (byte) 255);
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.CONCENTRATOR_SETUP.getClassId();
    }

}
