/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.common;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import java.math.BigDecimal;
import java.util.Collection;

public class MyOwnPrivateRegister implements OfflineRegister {

    private final OfflineDevice device;
    private final ObisCode obisCode;

    public MyOwnPrivateRegister(OfflineDevice device, ObisCode obisCode) {
        this.device = device;
        this.obisCode = obisCode;
    }

    @Override
    public long getRegisterId() {
        return 0;
    }

    @Override
    public ObisCode getObisCode() {
        return this.obisCode;
    }

    @Override
    public boolean inGroup(long registerGroupId) {
        return false;
    }

    @Override
    public boolean inAtLeastOneGroup(Collection<Long> registerGroupIds) {
        return false;
    }

    @Override
    public Unit getUnit() {
        return Unit.getUndefined();
    }

    @Override
    public String getDeviceMRID() {
        return null;
    }

    @Override
    public String getDeviceSerialNumber() {
        return this.device.getSerialNumber();
    }

    @Override
    public ObisCode getAmrRegisterObisCode() {
        return obisCode;
    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return this.device.getDeviceIdentifier();
    }

    @Override
    public ReadingType getReadingType() {
        return null;
    }

    @Override
    public BigDecimal getOverFlowValue() {
        return null;
    }

    @Override
    public boolean isText() {
        return true;
    }
}