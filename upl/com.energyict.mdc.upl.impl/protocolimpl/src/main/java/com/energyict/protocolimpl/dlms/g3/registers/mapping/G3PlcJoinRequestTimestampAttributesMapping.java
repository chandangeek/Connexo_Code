package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Created by H165680 on 17/04/2017.
 */
public class G3PlcJoinRequestTimestampAttributesMapping extends RegisterMapping {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.168.96.193.0.255");
    private static final int MIN_ATTR = 0;
    private static final int MAX_ATTR = 2;

    public G3PlcJoinRequestTimestampAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return OBIS_CODE.equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        return parse(obisCode, readAttribute(obisCode));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode) throws IOException {
        switch (obisCode.getE()) {
            case 0:
            case 2:
                getCosemObjectFactory().getData(OBIS_CODE).getAttrbAbstractDataType(2);
            default:
                throw new NoSuchRegisterException("G3 PLC Join Request Timestamp attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {
        switch (obisCode.getE()) {
            case 0:
            case 2:
                AXDRDateTime dateTime = new AXDRDateTime(abstractDataType.getOctetString());
                return new RegisterValue(obisCode, dateTime.getValue().getTime().toString());
            default:
                throw new NoSuchRegisterException("G3 PLC Join Request Timestamp attribute [" + obisCode.getE() + "] not supported!");

        }
    }

}
