/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.sync.KoreMeterConfigurationUpdater;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;

public class RegisterUpdaterImpl implements Register.RegisterUpdater {

    private final EventService eventService;
    private final Register register;
    private final ServerDeviceService deviceService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final Clock clock;

    private Integer overruledNbrOfFractionDigits;
    private BigDecimal overruledOverflowValue;
    private ObisCode overruledObisCode;

    RegisterUpdaterImpl(ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService, Register register) {
        this.deviceService = deviceService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.clock = clock;
        this.eventService = eventService;
        this.register = register;
    }

    public ReadingType getReadingType() {
        return register.getReadingType();
    }

    public Integer getOverruledNbrOfFractionDigits() {
        return overruledNbrOfFractionDigits;
    }

    @Override
    public Register.RegisterUpdater setNumberOfFractionDigits(Integer overruledNbrOfFractionDigits) {
        this.overruledNbrOfFractionDigits = overruledNbrOfFractionDigits;
        return this;
    }

    public BigDecimal getOverruledOverflowValue() {
        return overruledOverflowValue;
    }

    @Override
    public Register.RegisterUpdater setOverflowValue(BigDecimal overruledOverflowValue) {
        this.overruledOverflowValue = overruledOverflowValue;
        return this;
    }

    public ObisCode getOverruledObisCode() {
        return overruledObisCode;
    }

    @Override
    public Register.RegisterUpdater setObisCode(ObisCode overruledObisCode) {
        this.overruledObisCode = overruledObisCode;
        return this;
    }

    @Override
    public void update() {
        DeviceImpl device = (DeviceImpl) register.getDevice();
        if (register.getRegisterSpec() instanceof NumericalRegisterSpec) {
            //textRegisters don't have fraction digits and overflow values
            if (numberOfFractionDigitsHasChanged() || overflowValueHasChanged()) {
                device.syncWithKore(new KoreMeterConfigurationUpdater(this.deviceService, this.readingTypeUtilService, this.clock, eventService)
                        .withRegisterUpdater(this));
                device.executeSyncs();
            }
        }
        if (obisCodeHasChanged()) {
            new DeviceObisCodeUsageUpdater().update(device, getReadingType(), overruledObisCode);
        }
        device.validateForUpdate();
        device.postSave();
    }

    private boolean numberOfFractionDigitsHasChanged() {
        return (this.register instanceof NumericalRegister)
                && this.overruledNbrOfFractionDigits != null
                && ((NumericalRegister) this.register).getNumberOfFractionDigits() != this.overruledNbrOfFractionDigits;
    }

    private boolean overflowValueHasChanged() {
        return (this.register instanceof NumericalRegister)
                && this.overruledOverflowValue != null
                && !((NumericalRegister) this.register).getOverflow().equals(Optional.ofNullable(this.overruledOverflowValue));
    }

    private boolean obisCodeHasChanged() {
        return !this.register.getDeviceObisCode().equals(this.overruledObisCode);
    }
}
