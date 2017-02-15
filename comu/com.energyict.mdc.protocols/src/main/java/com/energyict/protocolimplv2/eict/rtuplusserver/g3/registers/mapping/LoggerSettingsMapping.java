/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.LoggerSettings;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;

import java.io.IOException;

public class LoggerSettingsMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 3;

    public LoggerSettingsMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        final ObisCode defaultObisCode = LoggerSettings.getDefaultObisCode();
        return defaultObisCode.equalsIgnoreBChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final LoggerSettings loggerSettings = getCosemObjectFactory().getLoggerSettings();
        return parse(obisCode, readAttribute(obisCode, loggerSettings));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, LoggerSettings loggerSettings) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return OctetString.fromObisCode(LoggerSettings.getDefaultObisCode());

            // Server log level
            case 2:
                return loggerSettings.getServerLogLevel();

            // Web portal log level
            case 3:
                return loggerSettings.getWebPortalLogLevel();

            default:
                throw new NoSuchRegisterException("LoggerSettingsMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {
        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, LoggerSettings.getDefaultObisCode().toString());

            // Server log level
            case 2:
                return new RegisterValue(obisCode, new Quantity(((TypeEnum) abstractDataType).getValue(), Unit.getUndefined()));

            // Web portal log level
            case 3:
                return new RegisterValue(obisCode, new Quantity(((TypeEnum) abstractDataType).getValue(), Unit.getUndefined()));

            default:
                throw new NoSuchRegisterException("LoggerSettingsMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }
}
