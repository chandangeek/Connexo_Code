/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GPRSModemSetup;
import com.energyict.dlms.cosem.attributeobjects.QualityOfService;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;

import java.io.IOException;

public class GprsModemSetupMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 4;

    public GprsModemSetupMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return GPRSModemSetup.getDefaultObisCode().equalsIgnoreBChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final GPRSModemSetup gprsModemSetup = getCosemObjectFactory().getGPRSModemSetup(GPRSModemSetup.getDefaultObisCode());
        return parse(obisCode, readAttribute(obisCode, gprsModemSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, GPRSModemSetup gprsModemSetup) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return OctetString.fromObisCode(GPRSModemSetup.getDefaultObisCode());

            // APN
            case 2:
                return gprsModemSetup.readAPN();

            // PIN code
            case 3:
                return gprsModemSetup.readPinCode();

            // Quality of service
            case 4:
                return gprsModemSetup.readQualityOfService();

            default:
                throw new NoSuchRegisterException("GprsModemSetupMapping attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, GPRSModemSetup.getDefaultObisCode().toString());

            // APN
            case 2:
                return new RegisterValue(obisCode, ((OctetString) abstractDataType).stringValue());

            // PIN code
            case 3:
                return new RegisterValue(obisCode, String.valueOf(abstractDataType.intValue()));

            // Quality of service
            case 4:
                return new RegisterValue(obisCode, ((QualityOfService) abstractDataType).toString());

            default:
                throw new NoSuchRegisterException("GprsModemSetupMapping attribute [" + obisCode.getB() + "] not supported!");

        }
    }
}