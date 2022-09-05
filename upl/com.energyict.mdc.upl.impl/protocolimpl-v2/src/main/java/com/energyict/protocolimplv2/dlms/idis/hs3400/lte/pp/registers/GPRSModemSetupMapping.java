package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class GPRSModemSetupMapping extends LteMapping{

    private GPRSModemSetupAttributeMapping gprsModemSetupAttributeMapping;

    protected GPRSModemSetupMapping(ObisCode obis) {
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

        if (gprsModemSetupAttributeMapping.canRead(getObisCode())) {
            return gprsModemSetupAttributeMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.GPRS_SETUP.getClassId();
    }


    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (gprsModemSetupAttributeMapping == null) {
            gprsModemSetupAttributeMapping = new GPRSModemSetupAttributeMapping(cosemObjectFactory);
        }
    }

    public int getAttributeNumber() {
        return getObisCode().getE();        //The E-field of the obiscode indicates which attribute is being read
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (gprsModemSetupAttributeMapping.canRead(obisCode)) {
            return gprsModemSetupAttributeMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }
}
