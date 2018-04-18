package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SNMPSetup;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Created by H165680 on 03/04/2017.
 */
public class SNMPSetupAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 2;
    private static final int MAX_ATTR = 10;

    public SNMPSetupAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return SNMPSetup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final SNMPSetup snmpSetup = getCosemObjectFactory().getSNMPSetup(obisCode);
        return parse(obisCode, readAttribute(obisCode, snmpSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, SNMPSetup snmpSetup) throws IOException {
        switch (obisCode.getE()) {
            case 2:
                return snmpSetup.readEnabledInterfaces();
            case 3:
                return snmpSetup.readUsers();
            case 4:
                return snmpSetup.readSystemContact();
            case 5:
                return snmpSetup.readSystemLocation();
            case 6:
                return snmpSetup.readLocalEngineId();
            case 7:
                return snmpSetup.readNotificationType();
            case 8:
                return snmpSetup.readNotificationUser();
            case 9:
                return snmpSetup.readNotificationHost();
            case 10:
                return snmpSetup.readNotificationPort();
            default:
                throw new NoSuchRegisterException("SNMP attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getE()) {
            case 2:
                return new RegisterValue(obisCode, getArrayDescription(abstractDataType.getArray()));
            case 3:
                return new RegisterValue(obisCode, getShortDescription(abstractDataType.getArray(), false));
            case 4:
                return new RegisterValue(obisCode, abstractDataType.getUtf8String().stringValue());
            case 5:
                return new RegisterValue(obisCode, abstractDataType.getUtf8String().stringValue());
            case 6:
                //TODO: check string value. seems to have a bad format
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 7:
                return new RegisterValue(obisCode, abstractDataType.getTypeEnum().getValue()+"");
            case 8:
                return new RegisterValue(obisCode, abstractDataType.getTypeEnum().getValue()+"");
            case 9:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 10:
                return new RegisterValue(obisCode, abstractDataType.getUnsigned16().getValue()+"");
            default:
                throw new NoSuchRegisterException("SNMP attribute [" + obisCode.getE() + "] not supported!");

        }
    }

}
