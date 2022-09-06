package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.HS3400LtePP;

import java.io.IOException;

public abstract class LteMapping {

    private final ObisCode obis;

    protected LteMapping(ObisCode obis) {
        this.obis = obis;
    }

    public final ObisCode getObisCode() {
        return obis;
    }

    public RegisterValue readRegister(HS3400LtePP hS3400LtePP) throws IOException {
        return readRegister(hS3400LtePP.getDlmsSession().getCosemObjectFactory());
    }

    public abstract RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException;

    public abstract RegisterValue parse(AbstractDataType abstractDataType) throws IOException;

    public ObisCode getBaseObisCode() {
        return getObisCode();
    }

    public int getValueAttribute(HS3400LtePP hS3400LtePP){
        return 2;
    }

    public int getDLMSClassId(){
        return DLMSClassId.GPRS_SETUP.getClassId();
    }
}
