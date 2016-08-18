package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 26.07.16
 * Time: 14:18
 */
public class SyncDeviceWithKoreForInfo extends AbstractSyncDeviceWithKoreMeter {

    private DeviceImpl device;
    private final Clock clock;
    private Optional<MeterActivation> currentMeterActivation = null;
    @IsPresent
    private Optional<Instant> initialMeterActivationStartDate = Optional.empty();

    public SyncDeviceWithKoreForInfo(DeviceImpl device, MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService) {
        super(meteringService, readingTypeUtilService, eventService, null);
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
            if (getDevice().getMeter().isPresent() && getDevice().getMeter()
                    .get()
                    .getCurrentMeterActivation()
                    .isPresent()) {
                this.currentMeterActivation = Optional.of(getDevice().getMeter()
                        .get()
                        .getCurrentMeterActivation()
                        .get());
            } else {
                this.currentMeterActivation = Optional.empty();
            }
        }
        return this.currentMeterActivation;
    }

    void setCurrentMeterActivation(Optional<MeterActivation> meterActivation) {
        this.currentMeterActivation = meterActivation;
    }

    public Optional<UsagePoint> getUsagePoint() {
        if (getCurrentMeterActivation().isPresent()) {
            return currentMeterActivation.get().getUsagePoint();
        } else {
            return Optional.empty();
        }
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
