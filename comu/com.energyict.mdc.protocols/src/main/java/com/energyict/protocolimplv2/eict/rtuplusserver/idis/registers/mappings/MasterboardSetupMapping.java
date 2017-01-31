/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.MasterboardSetup;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * @author sva
 * @since 15/10/2014 - 13:42
 */
public class MasterboardSetupMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 7;

    public MasterboardSetupMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        final ObisCode defaultObisCode = MasterboardSetup.getDefaultObisCode();
        return ProtocolTools.equalsIgnoreBField(defaultObisCode, obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final MasterboardSetup masterboardSetup = getCosemObjectFactory().getMasterboardSetup();
        return parse(obisCode, readAttribute(obisCode, masterboardSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, MasterboardSetup masterboardSetup) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return OctetString.fromObisCode(MasterboardSetup.getDefaultObisCode());

            // Local mac address
            case 3:
                return masterboardSetup.getLocalMacAddress();

            // Max credit
            case 4:
                return masterboardSetup.getMaxCredit();

            // Zero cross delay
            case 5:
                return masterboardSetup.getZeroCrossDelay();

            // Synchronisation bit
            case 6:
                return masterboardSetup.getSynchronisationBit();

            // Local system title
            case 7:
                return masterboardSetup.getLocalSystemTitle();

            default:
                throw new NoSuchRegisterException("MasterboardSetupMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }


    public RegisterValue parse(final ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, MasterboardSetup.getDefaultObisCode().toString());

            // Local mac address
            case 3:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            // Max credit
            case 4:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            // Zero cross delay
            case 5:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            // Synchronisation bit
            case 6:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            // Local system title
            case 7:
                return new RegisterValue(obisCode, ((OctetString) abstractDataType).stringValue());

            default:
                throw new NoSuchRegisterException("MasterboardSetupMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }
}