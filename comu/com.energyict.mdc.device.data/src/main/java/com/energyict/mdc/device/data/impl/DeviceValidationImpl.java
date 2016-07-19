package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.InvalidLastCheckedException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Ranges.does;
import static com.elster.jupiter.util.streams.Functions.asStream;

/**
 * Created by tgr on 9/09/2014.
 */
public class DeviceValidationImpl implements DeviceValidation {

    private final AmrSystem amrSystem;
    private final ValidationService validationService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final DeviceImpl device;
    private transient Meter meter;
    private transient ValidationEvaluator evaluator;

    public DeviceValidationImpl(AmrSystem amrSystem, ValidationService validationService, Clock clock, Thesaurus thesaurus, DeviceImpl device) {
        this.amrSystem = amrSystem;
        this.validationService = validationService;
        this.clock = clock;
        this.thesaurus = thesaurus;
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
    public boolean isValidationOnStorage() {
        return getEvaluator().isValidationOnStorageEnabled(fetchKoreMeter());
    }

    @Override
    public void activateValidationOnStorage(Instant lastChecked) {
        activateValidation(lastChecked, true);
    }

    @Override
    public void activateValidation(Instant lastChecked) {
        activateValidation(lastChecked, false);
    }

    void activateValidation(Instant lastChecked, boolean onStorage) {
        Meter koreMeter = this.fetchKoreMeter();
        if (koreMeter.hasData()) {
            if (lastChecked == null) {
                throw InvalidLastCheckedException.lastCheckedCannotBeNull(this.device, this.thesaurus, MessageSeeds.LAST_CHECKED_CANNOT_BE_NULL);
            }
            this.getMeterActivationsMostRecentFirst(koreMeter)
                    .filter(each -> this.isEffectiveOrStartsAfterLastChecked(lastChecked, each))
                    .forEach(each -> this.applyLastChecked(lastChecked, each));
        }
        this.validationService.activateValidation(koreMeter);
        if (onStorage) {
            this.validationService.enableValidationOnStorage(koreMeter);
        } else {
            this.validationService.disableValidationOnStorage(koreMeter);
        }
    }

    private boolean isEffectiveOrStartsAfterLastChecked(Instant lastChecked, MeterActivation meterActivation) {
        return meterActivation.isEffectiveAt(lastChecked) || meterActivation.getInterval().startsAfter(lastChecked);
    }

    private void applyLastChecked(Instant lastChecked, MeterActivation meterActivation) {
        ChannelsContainer channelsContainer = meterActivation.getChannelsContainer();
        Optional<Instant> channelsContainerLastChecked = validationService.getLastChecked(channelsContainer);
        if (meterActivation.isCurrent()) {
            if (channelsContainerLastChecked.isPresent() && lastChecked.isAfter(channelsContainerLastChecked.get())) {
                throw InvalidLastCheckedException.lastCheckedAfterCurrentLastChecked(this.device, channelsContainerLastChecked.get(), lastChecked, this.thesaurus, MessageSeeds.LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED);
            }
            this.validationService.updateLastChecked(channelsContainer, lastChecked);
        } else {
            Instant lastCheckedDateToSet = this.smallest(channelsContainerLastChecked.orElse(channelsContainer.getStart()), lastChecked);
            validationService.updateLastChecked(channelsContainer, lastCheckedDateToSet);
        }
    }

    private Instant smallest(Instant instant1, Instant instant2) {
        return Ordering.natural().min(instant1, instant2);
    }

    @Override
    public void deactivateValidation() {
        this.validationService.deactivateValidation(this.fetchKoreMeter());
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
    public boolean isValidationActive(Register<?, ?> register, Instant when) {
        if (!isValidationActive()) {
            return false;
        }
        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(register, when);
        return found.isPresent() ? evaluator.isValidationEnabled(found.get()) : hasActiveRules(register);
    }

    @Override
    public boolean allDataValidated(Channel channel, Instant when) {
        Optional<com.elster.jupiter.metering.Channel> found = device.findKoreChannel(channel, when);
        return !found.isPresent() || getEvaluator().isAllDataValidated(found.get().getChannelsContainer());
    }

    @Override
    public boolean allDataValidated(Register<?, ?> register, Instant when) {
        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(register, when);
        return !found.isPresent() || getEvaluator().isAllDataValidated(found.get().getChannelsContainer());
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
    public Optional<Instant> getLastChecked(Register<?, ?> register) {
        return getLastChecked(register.getReadingType());
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Range<Instant> interval) {
        Stream<com.elster.jupiter.metering.Channel> koreChannels = ((DeviceImpl) channel.getDevice()).findKoreChannels(channel).stream();
        return koreChannels
                .filter(k -> does(k.getChannelsContainer().getRange()).overlap(interval))
                .flatMap(k -> getEvaluator().getValidationStatus(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM), k, readings,
                        k.getChannelsContainer().getRange().intersection(interval)).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Register<?, ?> register, List<? extends BaseReading> readings, Range<Instant> interval) {
        return ((DeviceImpl) register.getDevice()).findKoreChannels(register).stream()
                .filter(k -> does(k.getChannelsContainer().getRange()).overlap(interval))
                .flatMap(k -> getEvaluator().getValidationStatus(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM), k, readings,
                        k.getChannelsContainer().getRange().intersection(interval)).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void validateData() {
        List<ChannelsContainer> channelsContainers = device.getMeterActivations()
                .stream()
                .map(MeterActivation::getChannelsContainer)
                .collect(Collectors.toList());
        if (!channelsContainers.isEmpty()) {
            Range<Instant> range = channelsContainers.get(0).getRange();
            ValidationEvaluator evaluator = this.validationService.getEvaluator(this.fetchKoreMeter(), range);
            channelsContainers.forEach(channelsContainer -> {
                if (!evaluator.isAllDataValidated(channelsContainer)) {
                    this.validationService.validate(EnumSet.of(QualityCodeSystem.MDC), channelsContainer);
                }
            });
        }
    }

    @Override
    public void validateLoadProfile(LoadProfile loadProfile) {
        loadProfile.getChannels().forEach(this::validateChannel);
    }

    @Override
    public void validateChannel(Channel channel) {
        validate(channel.getReadingType());
    }

    @Override
    public void validateRegister(Register<?, ?> register) {
        validate(register.getReadingType());
    }

    @Override
    public void setLastChecked(Channel channel, Instant start) {
        getDevice()
                .findKoreChannels(channel)
                .stream()
                .forEach(c -> this.validationService.updateLastChecked(c, start));
    }

    @Override
    public void setLastChecked(Register<?, ?> register, Instant start) {
        getDevice()
                .findKoreChannels(register)
                .stream()
                .forEach(c -> this.validationService.updateLastChecked(c, start));
    }

    private boolean hasActiveRules(Channel channel) {
        return hasActiveRules(channel.getReadingType());
    }

    private boolean hasActiveRules(Register<?, ?> register) {
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

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Register<?, ?> register, Instant when) {
        ReadingType readingType = register.getReadingType();
        return findKoreChannel(readingType, when);
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(ReadingType readingType, Instant when) {
        return fetchKoreMeter().getChannelsContainers()
                .stream()
                .filter(channelContainer -> channelContainer.getRange().contains(when))
                .flatMap(channelContainer -> channelContainer.getChannels().stream())
                .filter(channel -> channel.getReadingTypes().contains(readingType))
                .findFirst();
    }

    private Optional<Instant> getLastChecked(Meter meter) {
        return getMeterActivationsMostRecentFirst(meter)
                .map(MeterActivation::getChannelsContainer)
                .map(validationService::getLastChecked)  // may be use evaluator to allow caching this
                .flatMap(asStream())
                .findAny();
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
        return Comparator.comparing(MeterActivation::getRange, byStart());
    }

    private Comparator<? super Range<Instant>> byStart() {
        return (o1, o2) -> {
            if (!o1.hasLowerBound()) {
                return !o2.hasLowerBound() ? -1 : 0;
            }
            if (!o2.hasLowerBound()) {
                return 1;
            }
            return o1.lowerEndpoint().compareTo(o2.lowerEndpoint());
        };
    }

    private void validate(ReadingType readingType) {
        fetchKoreMeter().getChannelsContainers().stream()
                .forEach(channelContainer -> validationService.validate(EnumSet.of(QualityCodeSystem.MDC), channelContainer, readingType));
    }

    private Meter fetchKoreMeter() {
        if (meter == null) {
            if (device.getMeter().isPresent()){
                meter = device.getMeter().get();
            }else{
                throw new UnsupportedOperationException("No Kore Meter for device " + device.getId());
            }
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
