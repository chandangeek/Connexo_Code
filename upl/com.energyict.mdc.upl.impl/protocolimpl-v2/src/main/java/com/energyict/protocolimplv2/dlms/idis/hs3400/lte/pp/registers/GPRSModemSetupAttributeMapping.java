package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GPRSModemSetup;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;

import java.io.IOException;

public class GPRSModemSetupAttributeMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 4;

    public GPRSModemSetupAttributeMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(ObisCode obisCode) {
        return GPRSModemSetup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final GPRSModemSetup gprsModemSetup = getCosemObjectFactory().getGPRSModemSetup(obisCode);
        return parse(obisCode, readAttribute(obisCode, gprsModemSetup));
    }


    protected AbstractDataType readAttribute(final ObisCode obisCode, GPRSModemSetup gprsModemSetup) throws IOException {

        switch (obisCode.getE()) {
            case 1:
                return OctetString.fromObisCode(GPRSModemSetup.getDefaultObisCode());
            case 2:
                return gprsModemSetup.getAPN();
            case 3:
                return gprsModemSetup.getPinCod();
            case 4:
                return gprsModemSetup.getQualityOfService();
            default:
                throw new NoSuchRegisterException("GPRSModemSetup attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getE()) {
            case 1:
                return new RegisterValue(obisCode, GPRSModemSetup.getDefaultObisCode().toString());
            case 2:
                return new RegisterValue(obisCode, "APN: " + abstractDataType.getOctetString().stringValue());
            case 3:
                return new RegisterValue(obisCode, "PIN code: " + abstractDataType.longValue());
            case 4:
                return new RegisterValue(obisCode, "Quality of service: " + abstractDataType.toString());
            default:
                throw new NoSuchRegisterException("GPRSModemSetup attribute [" + obisCode.getE() + "] not supported!");

        }
    }

}
