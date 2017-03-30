/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that all data in load profiles is collected.
 * The actual check is done by comparing the last reading timestamp
 * of all profiles against the effective timestamp of the transition.
 * A missing last reading on a load profile
 * equals to a mismatch with the effective timestamp
 * and will therefore fail this check.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:48)
 */
public class AllLoadProfileDataCollected extends TranslatableServerMicroCheck {

    private final MeteringService meteringService;

    public AllLoadProfileDataCollected(Thesaurus thesaurus, MeteringService meteringService) {
        super(thesaurus);
        this.meteringService = meteringService;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp, State state) {
        return this.evaluateLoadProfiles(device, effectiveTimestamp);
    }

    private Optional<DeviceLifeCycleActionViolation> evaluateLoadProfiles(Device device, Instant effectiveTimestamp) {
        if (anyLoadProfileWithIncorrectLastReading(device, effectiveTimestamp).isPresent()) {
            return Optional.of(newViolation());
        }
        return Optional.empty();
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.ALL_LOAD_PROFILE_DATA_COLLECTED,
                MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED);
    }

    private Optional<LoadProfile> anyLoadProfileWithIncorrectLastReading(Device device, Instant effectiveTimestamp) {
        return device
                .getLoadProfiles()
                .stream()
                .filter(Predicates
                        .either(this::lastReadingInLoadProfileMissing)
                        .or(lp -> this.lastReadingOfLoadProfileIsNotFromPreviousInterval(lp, effectiveTimestamp)))
                .findAny();
    }

    private boolean lastReadingInLoadProfileMissing(LoadProfile loadProfile) {
        return loadProfile.getLastReading() == null;
    }

    private boolean lastReadingOfLoadProfileIsNotFromPreviousInterval(LoadProfile loadProfile, Instant effectiveTimestamp) {
        Instant loadProfileLastReading = loadProfile.getLastReading().toInstant();  //The null check already happened
        if (effectiveTimestamp.isAfter(loadProfileLastReading)) {
            final Instant[] nextIntervalForLoadProfile = {loadProfileLastReading};
            findMdcAmrSystem().findMeter(String.valueOf(loadProfile.getDevice().getId())).
                    ifPresent(meter -> meter.getCurrentMeterActivation()
                            .map(MeterActivation::getChannelsContainer)
                            .ifPresent(channelsContainer -> channelsContainer.getChannels()
                                    .stream()
                                    .filter(channel -> channel.getReadingTypes().contains(loadProfile.getChannels().get(0).getReadingType())
                                    ).findAny().ifPresent(result -> nextIntervalForLoadProfile[0] = result.getNextDateTime(loadProfileLastReading))));
            return effectiveTimestamp.isAfter(nextIntervalForLoadProfile[0]);
        } else {
            return false;
        }
    }

    private AmrSystem findMdcAmrSystem() {
        return this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
    }

    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED;
    }
}