package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.methods.USBSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exceptions.DataParseException;

import java.io.IOException;
import java.util.Date;

/**
 * Created by astor on 27.09.2016.
 */

public class USBSetupAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 4;

    public USBSetupAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return (USBSetup.getDefaultObisCode().equalsIgnoreBillingField(obisCode) || USBSetup.getLegacyObisCode().equalsIgnoreBillingField(obisCode)) &&
                (obisCode.getF() >= MIN_ATTR) &&
                (obisCode.getF() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final USBSetup usbSetup = getCosemObjectFactory().getUSBSetup(obisCode);
        return parse(obisCode, readAttribute(obisCode, usbSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, USBSetup usbSetup) throws IOException {
        switch (obisCode.getF()) {
            // Logical name
            case 1:
                return OctetString.fromObisCode(USBSetup.getDefaultObisCode());
            case 2:
                return usbSetup.readUSBState();
            case 3:
                return usbSetup.readUSBActivity();
            case 4:
                return usbSetup.readLastActivityTimeStamp();
            default:
                throw new NoSuchRegisterException("USBSetup attribute [" + obisCode.getF() + "] not supported!");
        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getF()) {
            // Logical name
            case 1:
                return new RegisterValue(obisCode, USBSetup.getDefaultObisCode().toString());
            case 2:
                boolean state =  abstractDataType.getBooleanObject().getState();
                return new RegisterValue(obisCode,  new Quantity(state?1:0, Unit.getUndefined()));
            case 3:
                boolean state1 =  abstractDataType.getBooleanObject().getState();
                return new RegisterValue(obisCode,  new Quantity(state1?1:0, Unit.getUndefined()));
            case 4:
                OctetString octetString = abstractDataType.getOctetString();
                Date dateTime = parseDateTime(octetString);
                return new RegisterValue(obisCode,  new Quantity(dateTime.getTime(), Unit.getUndefined()));
            default:
                throw new NoSuchRegisterException("USB Setup attribute [" + obisCode.getF() + "] not supported!");
        }
    }

    protected Date parseDateTime(OctetString octetString) {
        if (octetString == null) {
            throw DataParseException.ioException(new ProtocolException("Expected OctetString with DateTime format."));
        }
        try {
            return new AXDRDateTime(octetString).getValue().getTime();
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }
    }

}



