package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Ranges.does;
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
    public boolean isValidationActive(Channel channel, Instant when) {
        if (!isValidationActive()) {
            return false;
        }
        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(channel, when);
        return found.isPresent() ? evaluator.isValidationEnabled(found.get()) : hasActiveRules(channel);
    }

    @Override
    public boolean isValidationActive(Register<?> register, Instant when) {
        if (!isValidationActive()) {
            return false;
        }
        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(register, when);
        return found.isPresent() ? evaluator.isValidationEnabled(found.get()) : hasActiveRules(register);
    }

    @Override
    public boolean allDataValidated(Channel channel, Instant when) {
        Optional<com.elster.jupiter.metering.Channel> found = device.findKoreChannel(channel, when);
        return !found.isPresent() || getEvaluator().isAllDataValidated(found.get().getMeterActivation());
    }

    @Override
    public boolean allDataValidated(Register<?> register, Instant when) {
        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(register, when);
        return !found.isPresent() || getEvaluator().isAllDataValidated(found.get().getMeterActivation());
    }

    @Override
    public Optional<Instant> getLastChecked() {
        return getLastChecked(fetchKoreMeter());
    }

    @Override
    public Optional<Instant> getLastChecked(Channel channel) {
        return getLastChecked(channel.getReadingType());
    }

    @Override
    public Optional<Instant> getLastChecked(Register<?> register) {
        return getLastChecked(register.getReadingType());
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Range<Instant> interval) {
        Stream<com.elster.jupiter.metering.Channel> koreChannels = ((DeviceImpl) channel.getDevice()).findKoreChannels(channel).stream();
        return koreChannels
                .filter(k -> does(k.getMeterActivation().getRange()).overlap(interval))
                .flatMap(k -> getEvaluator().getValidationStatus(k, readings, k.getMeterActivation().getRange().intersection(interval)).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Register<?> register, List<? extends BaseReading> readings, Range<Instant> interval) {
        return ((DeviceImpl) register.getDevice()).findKoreChannels(register).stream()
                .filter(k -> does(k.getMeterActivation().getRange()).overlap(interval))
                .flatMap(k -> getEvaluator().getValidationStatus(k, readings, k.getMeterActivation().getRange().intersection(interval)).stream())
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
    public void validateLoadProfile(LoadProfile loadProfile) {
        loadProfile.getChannels().forEach(c -> this.validateChannel(c));
    }

    @Override
    public void validateChannel(Channel channel) {
        validate(channel.getReadingType());
    }

    @Override
    public void validateRegister(Register<?> register) {
        validate(register.getReadingType());
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
    public void setLastChecked(Channel channel, Instant start) {
        getDevice().findKoreChannels(channel).stream()
                .forEach(c -> {
                    validationService.updateLastChecked(c, start);
                });
    }

    @Override
    public void setLastChecked(Register<?> register, Instant start) {
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

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Channel channel, Instant when) {
        return findKoreChannel(channel.getReadingType(), when);
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Register<?> register, Instant when) {
        ReadingType readingType = register.getReadingType();
        return findKoreChannel(readingType, when);
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(ReadingType readingType, Instant when) {
        return fetchKoreMeter().getMeterActivations().stream()
                .filter(m -> m.getRange().contains(when)) // TODO verify with Karel
                .flatMap(m -> m.getChannels().stream())
                .filter(c -> c.getReadingTypes().contains(readingType))
                .findFirst();
    }

    private Optional<Instant> getLastChecked(Meter meter) {
        return getMeterActivationsMostRecentFirst(meter)
                .map(validationService::getLastChecked)  // may be use evaluator to allow caching this
                .filter(Optional::isPresent)
                .findAny()
                .flatMap(Function.identity());
    }

    private Optional<Instant> getLastChecked(ReadingType readingType) {
        return getEvaluator().getLastChecked(fetchKoreMeter(), readingType);
    }

    private Stream<MeterActivation> getMeterActivationsMostRecentFirst(Meter meter) {
        TreeSet<MeterActivation> meterActivations = new TreeSet<>(byInterval());
        meterActivations.addAll(meter.getMeterActivations());
        return meterActivations.stream();
    }

    private Comparator<MeterActivation> byInterval() {
        return (m1, m2) -> IntermittentInterval.IntervalComparators.FROM_COMPARATOR.compare(m1.getInterval(), m2.getInterval());
    }

    private Range<Instant> interval(List<? extends BaseReading> readings) {
        Instant min = readings.stream().map(BaseReading::getTimeStamp).min(naturalOrder()).get();
        Instant max = readings.stream().map(BaseReading::getTimeStamp).max(naturalOrder()).get();
        return Range.closed(min, max);
    }

    private void validate(ReadingType readingType) {
        fetchKoreMeter().getMeterActivations().stream()
                .forEach(meterActivation -> validationService.validate(meterActivation, readingType));
    }

    private Meter fetchKoreMeter() {
        if (meter == null) {
            meter = device.findOrCreateKoreMeter(amrSystem);
        }
        return meter;
    }

    private ValidationEvaluator getEvaluator() {
        if (evaluator == null) {
            evaluator = validationService.getEvaluator(fetchKoreMeter(), Range.atMost(clock.instant()));
        }
        return evaluator;
    }


}
