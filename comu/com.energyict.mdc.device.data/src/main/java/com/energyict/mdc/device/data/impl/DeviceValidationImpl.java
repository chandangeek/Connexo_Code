package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.google.common.base.Optional;
import com.google.common.collect.Ordering;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;

/**
 * Created by tgr on 9/09/2014.
 */
public class DeviceValidationImpl implements DeviceValidation {

    private final AmrSystem amrSystem;
    private final ValidationService validationService;
    private final DeviceImpl device;
    private Boolean validationActiveCached;
    private transient Meter meter;

    public DeviceValidationImpl(AmrSystem amrSystem, ValidationService validationService, DeviceImpl device) {
        this.amrSystem = amrSystem;
        this.validationService = validationService;
        this.device = device;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public boolean isValidationActive(Date when) {
        Optional<Meter> found = device.findKoreMeter(amrSystem);
        return found.isPresent() && validationService.validationEnabled(found.get());
    }

    private boolean isValidationActive(Date when, boolean useCached) {
        if (!useCached || validationActiveCached == null) {
            validationActiveCached = isValidationActive(when);
        }
        return validationActiveCached;
    }

    @Override
    public boolean isValidationActive(Channel channel, Date when) {
        if (!isValidationActive(when, true)) {
            return false;
        }
        Optional<com.elster.jupiter.metering.Channel> found = device.findKoreChannel(channel, when);
        return found.isPresent() && validationService.getMeterActivationValidations(found.get().getMeterActivation()).stream()
                .map(m -> m.getChannelValidation(found.get()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(ChannelValidation::hasActiveRules);
    }

    @Override
    public Optional<Date> getLastChecked(Channel channel) {
        return device.findKoreChannels(channel).stream()
                .filter(k -> k.getReadingTypes().contains(channel.getReadingType()))
                .map(validationService::getLastChecked)
                .findFirst()
                .orElse(Optional.<Date>absent());
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, Interval interval) {
        List<com.elster.jupiter.metering.Channel> koreChannels = ((DeviceImpl) channel.getDevice()).findKoreChannels(channel);
        return koreChannels.stream()
                .filter(k -> k.getMeterActivation().getInterval().overlaps(interval))
                .flatMap(k -> validationService.getEvaluator().getValidationStatus(k, k.getMeterActivation().getInterval().intersection(interval)).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void validateLoadProfile(LoadProfile loadProfile, Date start, Date until) {
        loadProfile.getChannels().stream()
                .forEach(c -> this.validateChannel(c, start, until));
    }

    @Override
    public void validateChannel(Channel channel, Date start, Date until) {
        validateReadingType(channel.getReadingType(), start, until);
    }

    private void validateReadingType(ReadingType readingType, Date start, Date until) {
        if (start != null) {
            doValidate(readingType, start, until);
            return;
        }
        doValidate(readingType, until);
    }

    private void doValidate(ReadingType readingType, Date until) {
        fetchKoreMeter().getMeterActivations().stream()
                .flatMap(m -> m.getChannels().stream())
                .filter(c -> c.getReadingTypes().contains(readingType))
                .map(c -> Pair.of(c, clippedInterval(c, until)))
                .forEach(p -> validationService.validate(p.getFirst().getMeterActivation(), readingType.getMRID(), p.getLast()));
    }

    private void doValidate(ReadingType readingType, Date start, Date until) {
        Interval interval = new Interval(start, until);
        fetchKoreMeter().getMeterActivations().stream()
                .filter(m -> m.getInterval().overlaps(interval))
                .flatMap(m -> m.getChannels().stream())
                .filter(c -> c.getReadingTypes().contains(readingType))
                .forEach(c -> validationService.validate(c.getMeterActivation(), readingType.getMRID(), clippedInterval(c, start, until)));
    }

    private Interval clippedInterval(com.elster.jupiter.metering.Channel c, Date start, Date until) {
        return new Interval(clippedStart(c, start), clippedEnd(c, until));
    }

    private Interval clippedInterval(com.elster.jupiter.metering.Channel c, Date until) {
        return new Interval(defaultStart(c), clippedEnd(c, until));
    }

    private Date clippedEnd(com.elster.jupiter.metering.Channel c, Date until) {
        return Ordering.<Date>from(nullsLast(naturalOrder())).min(until, c.getMeterActivation().getInterval().getEnd());
    }

    private Date defaultStart(com.elster.jupiter.metering.Channel channel) {
        return validationService.getLastChecked(channel).or(() -> channel.getMeterActivation().getInterval().getStart());
    }

    private Date clippedStart(com.elster.jupiter.metering.Channel channel, Date from) {
        return Ordering.<Date>from(nullsFirst(naturalOrder())).max(from, channel.getMeterActivation().getInterval().getStart());
    }

    private Meter fetchKoreMeter() {
        if (meter == null) {
            meter = device.findOrCreateKoreMeter(amrSystem);
        }
        return meter;
    }


}
