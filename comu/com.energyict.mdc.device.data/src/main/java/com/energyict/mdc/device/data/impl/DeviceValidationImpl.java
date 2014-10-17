package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Pair;
import java.time.Clock;import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import java.util.Optional;
import com.google.common.collect.Ordering;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.*;

/**
 * Created by tgr on 9/09/2014.
 */
public class DeviceValidationImpl implements DeviceValidation {

    private final AmrSystem amrSystem;
    private final ValidationService validationService;
    private final Clock clock;
    private final DeviceImpl device;
    private transient Meter meter;
    private transient ValidationEvaluator evaluator;

    public DeviceValidationImpl(AmrSystem amrSystem, ValidationService validationService, Clock clock, DeviceImpl device) {
        this.amrSystem = amrSystem;
        this.validationService = validationService;
        this.clock = clock;
        this.device = device;
    }

    @Override
    public DeviceImpl getDevice() {
        return device;
    }

    @Override
    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
        return getEvaluator().getValidationResult(qualities);
    }

    @Override
    public boolean isValidationActive() {
        return getEvaluator().isValidationEnabled(fetchKoreMeter());
    }

    @Override
    public boolean isValidationActive(Channel channel, Date when) {
        if (!isValidationActive()) {
            return false;
        }
        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(channel, when);
        return found.isPresent() ? evaluator.isValidationEnabled(found.get()) : hasActiveRules(channel);
    }

    @Override
    public boolean isValidationActive(Register<?> register, Date when) {
        if (!isValidationActive()) {
            return false;
        }
        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(register, when);
        return found.isPresent() ? evaluator.isValidationEnabled(found.get()) : hasActiveRules(register);
    }

    @Override
    public boolean allDataValidated(Channel channel, Date when) {
        Optional<com.elster.jupiter.metering.Channel> found = device.findKoreChannel(channel, when);
        return !found.isPresent() || getEvaluator().isAllDataValidated(found.get().getMeterActivation());
    }

    @Override
    public boolean allDataValidated(Register<?> register, Date when) {
        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(register, when);
        return !found.isPresent() || getEvaluator().isAllDataValidated(found.get().getMeterActivation());
    }

    @Override
    public Optional<Date> getLastChecked() {
        return getLastChecked(fetchKoreMeter());
    }

    @Override
    public Optional<Date> getLastChecked(Channel channel) {
        return getLastChecked(channel.getReadingType());
    }

    @Override
    public Optional<Date> getLastChecked(Register<?> register) {
        return getLastChecked(register.getReadingType());
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Interval interval) {
        Stream<com.elster.jupiter.metering.Channel> koreChannels = ((DeviceImpl) channel.getDevice()).findKoreChannels(channel).stream();
        return koreChannels
                .filter(k -> k.getMeterActivation().getInterval().overlaps(interval))
                .flatMap(k -> getEvaluator().getValidationStatus(k, readings, k.getMeterActivation().getInterval().intersection(interval)).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Register<?> register, List<? extends BaseReading> readings, Interval interval) {
        return ((DeviceImpl) register.getDevice()).findKoreChannels(register).stream()
                .filter(k -> k.getMeterActivation().getInterval().overlaps(interval))
                .flatMap(k -> getEvaluator().getValidationStatus(k, readings, k.getMeterActivation().getInterval().intersection(interval)).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings) {
        if (readings.isEmpty()) {
            return Collections.emptyList();
        }
        return getValidationStatus(channel, readings, interval(readings));
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Register<?> register, List<? extends BaseReading> readings) {
        if (readings.isEmpty()) {
            return Collections.emptyList();
        }
        return getValidationStatus(register, readings, interval(readings));
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

    @Override
    public void validateRegister(Register<?> register, Date start, Date until) {
        validateReadingType(register.getReadingType(), start, until);
    }

    @Override
    public boolean hasData(Channel channel) {
        return getDevice().findKoreChannels(channel).stream()
                .anyMatch(c -> c.hasData());
    }

    @Override
    public boolean hasData(Register<?> register) {
        return getDevice().findKoreChannels(register).stream()
                .anyMatch(c -> c.hasData());
    }

    @Override
    public void setLastChecked(Channel channel, Date start) {
        getDevice().findKoreChannels(channel).stream()
                .forEach(c -> {
                    validationService.updateLastChecked(c, start);
                });
    }

    @Override
    public void setLastChecked(Register<?> register, Date start) {
        getDevice().findKoreChannels(register).stream()
                .forEach(c -> {
                    validationService.updateLastChecked(c, start);
                });
    }

    private boolean hasActiveRules(Channel channel) {
        return hasActiveRules(channel.getReadingType());
    }

    private boolean hasActiveRules(Register<?> register) {
        return hasActiveRules(register.getReadingType());
    }

    private boolean hasActiveRules(ReadingType readingType) {
        return device.getDeviceConfiguration().getValidationRuleSets().stream()
                .flatMap(s -> s.getRules().stream())
                .anyMatch(r -> r.getReadingTypes().contains(readingType));
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Channel channel, Date when) {
        return findKoreChannel(channel.getReadingType(), when);
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Register<?> register, Date when) {
        ReadingType readingType = register.getReadingType();
        return findKoreChannel(readingType, when);
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(ReadingType readingType, Date when) {
        com.elster.jupiter.metering.Channel koreChannel = fetchKoreMeter().getMeterActivations().stream()
                .filter(m -> m.getInterval().contains(when, Interval.EndpointBehavior.CLOSED_CLOSED)) // TODO verify with Karel
                .flatMap(m -> m.getChannels().stream())
                .filter(c -> c.getReadingTypes().contains(readingType))
                .findFirst().orElse(null);
        return Optional.fromNullable(koreChannel);
    }

    private Optional<Date> getLastChecked(Meter meter) {
        for (MeterActivation meterActivation : getMeterActivationsMostRecentFirst(meter)) {
            Optional<Date> lastChecked = validationService.getLastChecked(meterActivation);
            if (lastChecked.isPresent()) {
                return lastChecked;
            }
        }
        return Optional.absent();
    }

    private Optional<Date> getLastChecked(ReadingType readingType) {
        return getEvaluator().getLastChecked(fetchKoreMeter(), readingType);
    }

    private Iterable<MeterActivation> getMeterActivationsMostRecentFirst(Meter meter) {
        TreeSet<MeterActivation> meterActivations = new TreeSet<>(byInterval());
        meterActivations.addAll(meter.getMeterActivations());
        return meterActivations;
    }

    private Comparator<MeterActivation> byInterval() {
        return (m1, m2) -> IntermittentInterval.IntervalComparators.FROM_COMPARATOR.compare(m1.getInterval(), m2.getInterval());
    }

    private Interval interval(List<? extends BaseReading> readings) {
        Date min = readings.stream().map(BaseReading::getTimeStamp).min(naturalOrder()).get();
        Date max = readings.stream().map(BaseReading::getTimeStamp).max(naturalOrder()).get();
        return new Interval(min, max);
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
                .map(c -> Pair.of(c, clippedInterval(c, readingType, until)))
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

    private Interval clippedInterval(com.elster.jupiter.metering.Channel c, ReadingType readingType, Date until) {
        return new Interval(defaultStart(c, readingType), clippedEnd(c, until));
    }

    private Date clippedEnd(com.elster.jupiter.metering.Channel c, Date until) {
        return Ordering.<Date>from(nullsLast(naturalOrder())).min(until, c.getMeterActivation().getInterval().getEnd());
    }

    private Date defaultStart(com.elster.jupiter.metering.Channel channel, ReadingType readingType) {
        return getEvaluator().getLastChecked(fetchKoreMeter(), readingType).or(() -> firstReading(channel));
    }

    private Date clippedStart(com.elster.jupiter.metering.Channel channel, Date from) {
        return Ordering.<Date>from(nullsFirst(naturalOrder())).max(from, firstReading(channel));
    }

    private Date firstReading(com.elster.jupiter.metering.Channel channel) {
        int minutes = channel.getMainReadingType().getMeasuringPeriod().getMinutes();
        Instant start = channel.getMeterActivation().getInterval().getStart().toInstant();
        return Date.from(start.plus(minutes, ChronoUnit.MINUTES));
    }

    private Meter fetchKoreMeter() {
        if (meter == null) {
            meter = device.findOrCreateKoreMeter(amrSystem);
        }
        return meter;
    }

    private ValidationEvaluator getEvaluator() {
        if (evaluator == null) {
            evaluator = validationService.getEvaluator(fetchKoreMeter(), Interval.endAt(Date.from(clock.instant())));
        }
        return evaluator;
    }


}
