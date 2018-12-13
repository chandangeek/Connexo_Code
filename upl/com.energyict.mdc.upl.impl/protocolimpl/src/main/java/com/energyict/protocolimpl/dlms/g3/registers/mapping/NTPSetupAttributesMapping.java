package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.NTPSetup;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Created by H245796 on 18.12.2017.
 */
public class NTPSetupAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 4;

    public NTPSetupAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return NTPSetup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final NTPSetup ntpSetup = getCosemObjectFactory().getNTPSetup(obisCode);
        return parse(obisCode, readAttribute(obisCode, ntpSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, NTPSetup ntpSetup) throws IOException {
        switch (obisCode.getB()) {
            case 1:
                return ntpSetup.readLogicalName();
            case 2:
                return ntpSetup.readActivated();
            case 3:
                return ntpSetup.readServerAddress();
            case 4:
                return ntpSetup.readServerPort();
            default:
                throw new NoSuchRegisterException("NTPSetup attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {
            case 1:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 2:
                return new RegisterValue(obisCode, abstractDataType.getBooleanObject().toString());
            case 3:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 4:
                return new RegisterValue(obisCode, "" + abstractDataType.getUnsigned16().getValue());
            default:
                throw new NoSuchRegisterException("NTPSetup attribute [" + obisCode.getB() + "] not supported!");

        }
    }

}
