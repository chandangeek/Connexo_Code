/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.NetworkManagement;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * @author sva
 * @since 15/10/2014 - 13:42
 */
public class NetworkManagementMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 6;

    public NetworkManagementMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        final ObisCode defaultObisCode = NetworkManagement.getDefaultObisCode();
        return ProtocolTools.equalsIgnoreBField(defaultObisCode, obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final NetworkManagement networkManagement = getCosemObjectFactory().getNetworkManagement();
        return parse(obisCode, readAttribute(obisCode, networkManagement));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, NetworkManagement networkManagement) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return OctetString.fromObisCode(NetworkManagement.getDefaultObisCode());

            // Discover duration
            case 2:
                return networkManagement.getDiscoverDuration();

            // Discover interval
            case 3:
                return networkManagement.getDiscoverInterval();

            // Repeater call interval
            case 4:
                return networkManagement.getRepeaterCallInterval();

            // Repeater call threshold
            case 5:
                return networkManagement.getRepeaterCallThreshold();

            // Repeater call timeslots new systems
            case 6:
                return networkManagement.getRepeaterCallTimeslots();

            default:
                throw new NoSuchRegisterException("NetworkManagementMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, NetworkManagement.getDefaultObisCode().toString());

            // Discover duration
            case 2:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.MINUTE)));

            // Discover interval
            case 3:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.get(BaseUnit.HOUR)));

            // Repeater call interval
            case 4:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.MINUTE)));

            // Repeater call threshold
            case 5:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            // Repeater call timeslots new systems
            case 6:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.intValue(), Unit.getUndefined()));

            default:
                throw new NoSuchRegisterException("NetworkManagementMapping attribute [" + obisCode.getB() + "] not supported!");
        }
    }
}