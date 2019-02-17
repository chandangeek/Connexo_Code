/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.lifecycle.EvaluableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Checks that all data in load profiles is collected.
 * The actual check is done by comparing the last reading timestamp
 * of all profiles against the effective timestamp of the transition.
 * A missing last reading on a load profile
 * equals to a mismatch with the effective timestamp
 * and will therefore fail this check.
 */
public class AllLoadProfileDataCollected extends TranslatableServerMicroCheck {

    private MeteringService meteringService;

    @Inject
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public String getCategory() {
        return MicroCategory.DATA_COLLECTION.name();
    }

    @Override
    public Optional<EvaluableMicroCheckViolation> evaluate(Device device, Instant effectiveTimestamp) {
        return anyLoadProfileWithIncorrectLastReading(device, effectiveTimestamp).isPresent() ?
                violationFailed(MicroCheckTranslationKeys.MICRO_CHECK_MESSAGE_ALL_LOAD_PROFILE_DATA_COLLECTED) :
                Optional.empty();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.of(
                DefaultTransition.DEACTIVATE,
                DefaultTransition.DEACTIVATE_AND_DECOMMISSION,
                DefaultTransition.DECOMMISSION);
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
}