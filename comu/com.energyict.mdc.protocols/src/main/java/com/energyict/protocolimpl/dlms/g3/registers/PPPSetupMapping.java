package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PPPSetupAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Copywrite EnergyICT 22.09.2016.
 */

public class PPPSetupMapping extends G3Mapping {


    private PPPSetupAttributesMapping pppSetupAttributesMapping;

    protected PPPSetupMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    //Set the B-Filed to 0
    public ObisCode getBaseObisCode() {                 //Set the B-Filed to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 1, (byte) 0);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (pppSetupAttributesMapping == null) {
            pppSetupAttributesMapping = new PPPSetupAttributesMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        int attributeValue; // indicates which attribute is being read
        switch (getObisCode().getB()) {
            case 1: attributeValue = 1;
                break;
            case 2: attributeValue = 2;
                break;
            case 3: attributeValue = 3;
                break;
            case 4: attributeValue = 4;
                break;
            case 5: attributeValue = 5;
                break;
            case 6: attributeValue = -1;
                break;
            default: attributeValue = 0;
        }
        return attributeValue;
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (pppSetupAttributesMapping.canRead(getObisCode())) {
            return pppSetupAttributesMapping.parse(getObisCode(), abstractDataType);
        }

        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (pppSetupAttributesMapping.canRead(obisCode)) {
            return pppSetupAttributesMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if(getObisCode().equalsIgnoreBChannel(PPPSetup.getDefaultObisCode()) ){
            return DLMSClassId.PPP_SETUP.getClassId();
        } else {
            return -1;
        }
    }
}

