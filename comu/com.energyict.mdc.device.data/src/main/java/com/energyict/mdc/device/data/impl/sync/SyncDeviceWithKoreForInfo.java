/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class SyncDeviceWithKoreForInfo extends AbstractSyncDeviceWithKoreMeter {

    private DeviceImpl device;
    private final Clock clock;
    private Optional<MeterActivation> currentMeterActivation = null;
    private Optional<Instant> initialMeterActivationStartDate = Optional.empty();

    public SyncDeviceWithKoreForInfo(DeviceImpl device, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService) {
        super(deviceService, readingTypeUtilService, eventService, null);
        this.device = device;
        this.clock = clock;
    }

    public Optional<Instant> getInitialMeterActivationStartDate() {
        return initialMeterActivationStartDate;
    }

    public void setInitialMeterActivationStartDate(Instant initialMeterActivationStartDate) {
        if (initialMeterActivationStartDate != null) {
            this.initialMeterActivationStartDate = Optional.ofNullable(generalizeDatesToMinutes(initialMeterActivationStartDate));
        } else {
            this.initialMeterActivationStartDate = Optional.empty();
        }
    }

    @Override
    MeterActivation doActivateMeter(Instant generalizedStartDate) {
        super.setDevice(device);
        this.currentMeterActivation = Optional.of(this.endMeterActivationAndRestart(generalizedStartDate, getCurrentMeterActivation(), Optional
                .empty()));
        return currentMeterActivation.get();
    }

    public void deactivateMeter(Instant when) {
        this.getCurrentMeterActivation().ifPresent(meterActivation -> {
            meterActivation.endAt(when);
            this.currentMeterActivation = null;
        });
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        super.setDevice(device);
    }

    public Optional<MeterActivation> getCurrentMeterActivation() {
        if (this.currentMeterActivation == null) {
            if (getDevice().getMeter().isPresent()) {
                this.currentMeterActivation = getDevice()
                        .getMeter().get()
                        .getCurrentMeterActivation()
                        .map(MeterActivation.class::cast);
            } else {
                this.currentMeterActivation = Optional.empty();
            }
        }
        return this.currentMeterActivation;
    }

    void setCurrentMeterActivation(Optional<MeterActivation> meterActivation) {
        this.currentMeterActivation = meterActivation;
    }

    public void reloadCurrentMeterActivation() {
        this.currentMeterActivation = null;
    }

    public Optional<UsagePoint> getUsagePoint() {
        return getCurrentMeterActivation()
                .flatMap(MeterActivation::getUsagePoint);
    }

    public Optional<BigDecimal> getMultiplier() {
        return getMultiplier(getCurrentMeterActivation());
    }

    public Optional<BigDecimal> getMultiplierAt(Instant multiplierEffectiveTimeStamp) {
        return getMultiplier(this.getDevice().getMeter().get().getMeterActivation(multiplierEffectiveTimeStamp));
    }

    public Instant getMultiplierEffectiveTimeStamp() {
        List<MeterActivation> meterActivationsMostRecentFirst = device.getMeterActivationsMostRecentFirst();
        Instant effectiveTimeStamp = clock.instant();
        Optional<BigDecimal> currentMultiplier = getMultiplier();
        for (MeterActivation meterActivation : meterActivationsMostRecentFirst) {
            Optional<BigDecimal> multiplier = getMultiplier(Optional.of(meterActivation));
            if (multiplier.isPresent()) {
                if (currentMultiplier.get().compareTo(multiplier.get()) == 0) {
                    effectiveTimeStamp = meterActivation.getStart();
                } else {
                    break;
                }
            }
        }
        return effectiveTimeStamp;
    }

    @Override
    public boolean canUpdateCurrentMeterActivation() {
        return false;
    }
}
