package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GSMDiagnosticsIC;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Created by H165680 on 17/04/2017.
 */
public class GSMDiagnosticsAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 2;
    private static final int MAX_ATTR = 252;

    public GSMDiagnosticsAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return GSMDiagnosticsIC.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final GSMDiagnosticsIC gsmDiagnosticsIC = getCosemObjectFactory().getGSMDiagnosticsIC(obisCode);
        return parse(obisCode, readAttribute(obisCode, gsmDiagnosticsIC));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, GSMDiagnosticsIC gsmDiagnosticsIC) throws IOException {
        switch (obisCode.getE()) {
            case 2:
                return gsmDiagnosticsIC.readOperator();
            case 3:
                return gsmDiagnosticsIC.readStatus();
            case 5:
                return gsmDiagnosticsIC.readPSStatus();
            case 252:
                return gsmDiagnosticsIC.readIMEI();
            case 251:
                return gsmDiagnosticsIC.readIMSI();
            case 250:
                return gsmDiagnosticsIC.readSimCardId();
            default:
                throw new NoSuchRegisterException("GSM Diagnostics attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getE()) {
            case 2:
                return new RegisterValue(obisCode, abstractDataType.getVisibleString().getStr());
            case 3:
                return new RegisterValue(obisCode, "" + abstractDataType.getTypeEnum().getValue());
            case 5:
                return new RegisterValue(obisCode, "" + abstractDataType.getTypeEnum().getValue());
            case 252:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 251:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 250:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            default:
                throw new NoSuchRegisterException("GSM Diagnostics attribute [" + obisCode.getE() + "] not supported!");

        }
    }

}
