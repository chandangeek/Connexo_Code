package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.sync.KoreMeterConfigurationUpdater;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Clock;

/**
 * Copyrights EnergyICT
 * Date: 2/06/2016
 * Time: 14:12
 */
public class RegisterUpdaterImpl implements Register.RegisterUpdater {

    private final EventService eventService;
    private final Register register;
    private final MeteringService meteringService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final Clock clock;

    private Integer overruledNbrOfFractionDigits;
    private BigDecimal overruledOverflowValue;
    private ObisCode overruledObisCode;

    RegisterUpdaterImpl(MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService, Register register) {
        this.meteringService = meteringService;
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
        if (register.getRegisterSpec() instanceof NumericalRegisterSpec) { //textRegisters don't have fraction digits and overflow values
            if (this.overruledNbrOfFractionDigits != null || this.overruledOverflowValue != null) {
                device.syncWithKore(new KoreMeterConfigurationUpdater(this.meteringService, this.readingTypeUtilService, this.clock, eventService)
                        .withRegisterUpdater(this));
                device.executeSyncs();
            }
        }
        if (this.overruledObisCode != null) {
            new DeviceObisCodeUsageUpdater().update(device, getReadingType(), overruledObisCode);
        }
        device.validateForUpdate();
        device.postSave();
    }
}
