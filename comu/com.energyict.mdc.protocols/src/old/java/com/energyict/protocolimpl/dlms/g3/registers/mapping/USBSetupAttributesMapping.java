/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.exceptions.DataParseException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.methods.USBSetup;

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
        return USBSetup.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final USBSetup usbSetup = getCosemObjectFactory().getUSBSetup(obisCode);
        return parse(obisCode, readAttribute(obisCode, usbSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, USBSetup usbSetup) throws IOException {
        switch (obisCode.getB()) {
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
                throw new NoSuchRegisterException("USBSetup attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {
            // Logical name
            case 1:
                return new RegisterValue(obisCode, USBSetup.getDefaultObisCode().toString());
            case 2:
                return new RegisterValue(obisCode, "USB state: " + abstractDataType.getBooleanObject().getState());
            case 3:
                return new RegisterValue(obisCode, "USB activity: " + abstractDataType.getBooleanObject().getState());
            case 4:
                OctetString octetString = abstractDataType.getOctetString();
                Date dateTime = parseDateTime(octetString);
                return new RegisterValue(obisCode, "Last activity timestamp:" + dateTime.toString());
            default:
                throw new NoSuchRegisterException("USB Setup attribute [" + obisCode.getB() + "] not supported!");
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



