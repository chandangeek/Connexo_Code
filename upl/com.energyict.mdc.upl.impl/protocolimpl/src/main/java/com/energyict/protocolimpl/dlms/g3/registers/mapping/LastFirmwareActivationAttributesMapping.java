package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.LastFirmwareActivationMapping;

import java.io.IOException;

/**
 *
 */
public class LastFirmwareActivationAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 2;
    private static final int MAX_ATTR = 2;

    public LastFirmwareActivationAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return LastFirmwareActivationMapping.OBISCODE.equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        return parse(obisCode, readAttribute(obisCode));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode) throws IOException {
        switch (obisCode.getE()) {
            case 2:
                getCosemObjectFactory().getData(LastFirmwareActivationMapping.OBISCODE).getAttrbAbstractDataType(obisCode.getE());
            default:
                throw new NoSuchRegisterException("LastFirmwareActivationMapping attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getE()) {
            case 2:
                return new RegisterValue(obisCode, "Image ID: " + abstractDataType.getStructure().getDataType(0).getOctetString().stringValue());
            default:
                throw new NoSuchRegisterException("LastFirmwareActivation attribute [" + obisCode.getE() + "] not supported!");

        }
    }

}
