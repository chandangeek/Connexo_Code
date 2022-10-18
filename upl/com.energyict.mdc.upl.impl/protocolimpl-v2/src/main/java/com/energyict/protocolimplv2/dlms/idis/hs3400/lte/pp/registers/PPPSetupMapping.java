package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.HS3400LtePP;

import java.io.IOException;

public class PPPSetupMapping extends LteMapping {

    private PPPSetupMappingAttributeMapping pppSetupMappingAttributeMapping;

    protected PPPSetupMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public ObisCode getBaseObisCode() {                 //Set the E-Filed to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 4, (byte) 0);
    }

    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType) throws IOException {
        instantiateMappers(null);  //Not used here

        if (pppSetupMappingAttributeMapping.canRead(getObisCode())) {
            return pppSetupMappingAttributeMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if (getObisCode().equalsIgnoreBAndEChannel(PPPSetup.getDefaultObisCode())) {
            return DLMSClassId.PPP_SETUP.getClassId();
        } else {
            return -1;
        }
    }

    public int getValueAttribute(HS3400LtePP hS3400LtePP){
        return 5;
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (pppSetupMappingAttributeMapping == null) {
            pppSetupMappingAttributeMapping = new PPPSetupMappingAttributeMapping(cosemObjectFactory);
        }
    }

    public int getAttributeNumber() {
        return getObisCode().getE();
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (pppSetupMappingAttributeMapping.canRead(obisCode)) {
            return pppSetupMappingAttributeMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }
}
