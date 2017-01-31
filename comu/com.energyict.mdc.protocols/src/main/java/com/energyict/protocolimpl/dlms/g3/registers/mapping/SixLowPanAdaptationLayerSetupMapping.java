/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SixLowPanAdaptationLayerSetup;

import java.io.IOException;

public class SixLowPanAdaptationLayerSetupMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 19;

    public SixLowPanAdaptationLayerSetupMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }


    @Override
    public boolean canRead(final ObisCode obisCode) {
        return SixLowPanAdaptationLayerSetup.getDefaultObisCode().equalsIgnoreBChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final SixLowPanAdaptationLayerSetup sixLowPanSetup = getCosemObjectFactory().getSixLowPanAdaptationLayerSetup();
        return parse(obisCode, readAttribute(obisCode, sixLowPanSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, SixLowPanAdaptationLayerSetup sixLowPanSetup) throws IOException {

        switch (obisCode.getB()) {

            case 1:
                return OctetString.fromObisCode(SixLowPanAdaptationLayerSetup.getDefaultObisCode());

            case 2:
                return sixLowPanSetup.readAdpMaxHops();

            case 3:
                return sixLowPanSetup.readAdpWeakLQIValue();

            case 4:
                return sixLowPanSetup.readSecurityLevel();

            case 5:
                return sixLowPanSetup.readPrefixTable();

            case 6:
                return sixLowPanSetup.readAdpRoutingConfiguration();

            case 7:
                return sixLowPanSetup.readAdpBroadcastLogTableEntryTTL();

            case 8:
                return sixLowPanSetup.readAdpRoutingTable();

            case 9:
                return sixLowPanSetup.readContextInformationTable();

            case 10:
                return sixLowPanSetup.readBlacklistTable();

            case 11:
                return sixLowPanSetup.readBroadcastLogTable();

            case 12:
                return sixLowPanSetup.readGroupTable();

            case 13:
                return sixLowPanSetup.readMaxJoinWaitTime();

            case 14:
                return sixLowPanSetup.readPathDiscoveryTime();

            case 15:
                return sixLowPanSetup.readActiveKeyIndex();

            case 16:
                return sixLowPanSetup.readMetricType();

            case 17:
                return sixLowPanSetup.readCoordShortAddress();

            case 18:
                return sixLowPanSetup.readDisableDefaultRouting();

            case 19:
                return sixLowPanSetup.readDeviceType();

            default:
                throw new NoSuchRegisterException("SixLowPanAdaptationLayerSetupMapping attribute [" + obisCode.getB() + "] not supported!");

        }
        }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {

            case 1:
                return new RegisterValue(obisCode, SixLowPanAdaptationLayerSetup.getDefaultObisCode().toString());

            case 2:
            case 3:
            case 4:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            case 5:        //TODO test this description of array of structures
            case 6:       //TODO test this description of a structure
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return new RegisterValue(obisCode, getShortDescription((Array) abstractDataType));

            case 7:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.get(BaseUnit.MINUTE)));

            case 13:
            case 14:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.get(BaseUnit.SECOND)));

            default:
                throw new NoSuchRegisterException("SixLowPanAdaptationLayerSetupMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }
}