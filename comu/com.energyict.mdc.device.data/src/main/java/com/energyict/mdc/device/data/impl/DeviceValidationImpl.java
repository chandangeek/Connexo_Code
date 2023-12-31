/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.RangeComparatorFactory;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.ChannelValidationRuleOverriddenProperties;
import com.energyict.mdc.common.device.data.DeviceValidation;
import com.energyict.mdc.common.device.data.InvalidLastCheckedException;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.data.impl.properties.ChannelValidationRuleOverriddenPropertiesImpl;
import com.energyict.mdc.device.data.impl.properties.ValidationEstimationRuleOverriddenPropertiesImpl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Ranges.does;
import static com.elster.jupiter.util.streams.Functions.asStream;

class DeviceValidationImpl implements DeviceValidation {

    private static final Comparator<MeterActivation> MOST_RECENT_FIRST =
            Comparator.comparing(MeterActivation::getRange, RangeComparatorFactory.INSTANT_DEFAULT).reversed();

    private DataModel dataModel;
    private final ValidationService validationService;
    private final Thesaurus thesaurus;
    private final DeviceImpl device;
    private final Clock clock;
    private transient Meter meter;
    private transient ValidationEvaluator evaluator;

    DeviceValidationImpl(DataModel dataModel, ValidationService validationService, Thesaurus thesaurus, DeviceImpl device, Clock clock) {
        this.dataModel = dataModel;
        this.validationService = validationService;
        this.thesaurus = thesaurus;
        this.device = device;
        this.clock = clock;
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

    private void activateValidation(Instant lastChecked, boolean onStorage) {
        Meter koreMeter = this.fetchKoreMeter();
        if (koreMeter.hasData()) {
            if (lastChecked == null) {
                throw InvalidLastCheckedException.lastCheckedCannotBeNull(this.device, this.thesaurus, MessageSeeds.LAST_CHECKED_CANNOT_BE_NULL);
            }
            koreMeter.getMeterActivations().stream()
                    .filter(each -> isEffectiveOrStartsAfterLastChecked(lastChecked, each))
                    .forEach(each -> applyLastChecked(lastChecked, each));
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
            validationService.updateLastChecked(channelsContainer, lastChecked);
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
        return found.isPresent() ? evaluator.isValidationEnabled(found.get()) : hasSameRule(channel);
    }

    @Override
    public boolean isChannelStatusActive(Channel channel) {
        return hasActiveRule(channel);
    }

    @Override
    public boolean isChannelStatusActive(Register<?, ?> register) {
        return hasActiveRule(register);
    }

    @Override
    public boolean isValidationActive(Register<?, ?> register, Instant when) {
        if (!isValidationActive()) {
            return false;
        }
        Optional<com.elster.jupiter.metering.Channel> found = findKoreChannel(register, when);
        return found.isPresent() ? evaluator.isValidationEnabled(found.get()) : hasSameRule(register);
    }

    @Override
    public boolean allDataValidated(Channel channel, Instant when) {
        Optional<com.elster.jupiter.metering.Channel> found = device.findKoreChannel(channel, when);
        return !found.isPresent() || getEvaluator().isAllDataValidated(found.get().getChannelsContainer());
    }

    @Override
    public boolean allDataValidated(Channel channel) {
        List<com.elster.jupiter.metering.Channel> filteredChannels = device.findKoreChannels(channel).stream()
                .filter(com.elster.jupiter.metering.Channel::hasData).collect(Collectors.toList());
        boolean validated = false;
        if(filteredChannels.size() > 0){
            validated = filteredChannels.stream().allMatch(kore -> getEvaluator().isAllDataValidated(kore.getChannelsContainer()));
        }
        return validated;
    }

    @Override
    public boolean allDataValidated(Register<?, ?> register) {
        return device.findKoreChannels(register).stream().filter(com.elster.jupiter.metering.Channel::hasData).allMatch(kore -> getEvaluator().isAllDataValidated(kore.getChannelsContainer()));
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
    public Optional<Instant> getLastValidationRun() {
        return getLastValidationRun(fetchKoreMeter());
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
                .filter(k -> does(k.getChannelsContainer().getInterval().toOpenClosedRange()).overlap(interval))
                .flatMap(k -> getEvaluator().getValidationStatus(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM), k, readings,
                        k.getChannelsContainer().getInterval().toOpenClosedRange().intersection(interval)).stream())
                .collect(Collectors.toList());
    }

    @Override
    public DataValidationStatus getValidationStatus(Channel channel, Instant timeStamp, List<ReadingQualityRecord> readingQualities, Range<Instant> interval) {
        Stream<com.elster.jupiter.metering.Channel> koreChannels = ((DeviceImpl) channel.getDevice()).findKoreChannels(channel).stream();
        return koreChannels
                .filter(k -> does(k.getChannelsContainer().getRange()).overlap(interval))
                .map(k -> getEvaluator().getValidationStatus(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM), k, timeStamp, readingQualities))
                .collect(Collectors.toList()).get(0);
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
    public List<DataValidationStatus> getHistoryValidationStatus(Register<?, ?> register, List<? extends BaseReading> readings, List<ReadingQualityRecord> readingQualities, Range<Instant> interval) {
        return ((DeviceImpl) register.getDevice()).findKoreChannels(register).stream()
                .filter(k -> does(k.getChannelsContainer().getRange()).overlap(interval))
                .flatMap(k -> getEvaluator().getHistoryValidationStatus(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM), k, readings, readingQualities, interval)
                        .stream())
                .collect(Collectors.toList());
    }

    @Override
    public void validateData() {
        List<ChannelsContainer> channelsContainers = device.getMeterActivations()
                .stream()
                .map(MeterActivation::getChannelsContainer)
                .collect(Collectors.toList());
        if (!channelsContainers.isEmpty()) {
            ValidationEvaluator evaluator = this.validationService.getEvaluator(fetchKoreMeter());
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
    public void validateChannel(Channel channel, Range<Instant> range) {
        fetchKoreMeter().getChannelsContainers().stream()
                .forEach(channelsContainer -> this.validationService.validate(
                        new ValidationContextImpl(ImmutableSet.of(QualityCodeSystem.MDC), channelsContainer, channel.getReadingType()), range));
    }

    @Override
    public void validateRegister(Register<?, ?> register) {
        validate(register.getReadingType());
    }

    @Override
    public void setLastChecked(Channel channel, Instant start) {
        getDevice().findKoreChannels(channel)
                .forEach(c -> this.validationService.updateLastChecked(c, start));
    }

    @Override
    public void setLastChecked(Register<?, ?> register, Instant start) {
        getDevice().findKoreChannels(register)
                .forEach(c -> this.validationService.updateLastChecked(c, start));
    }

    private boolean hasActiveRule(Channel channel) {
        if (channel.getCalculatedReadingType(clock.instant()).isPresent()) {
            return hasActiveRule(channel.getReadingType()) || hasActiveRule(channel.getCalculatedReadingType(clock.instant()).get());
        } else {
            return hasActiveRule(channel.getReadingType());
        }
    }

    private boolean hasActiveRule(Register<?, ?> register) {
        if (register.getCalculatedReadingType(clock.instant()).isPresent()) {
            return hasActiveRule(register.getReadingType()) || hasActiveRule(register.getCalculatedReadingType(clock.instant()).get());
        } else {
            return hasActiveRule(register.getReadingType());
        }
    }

    private boolean hasActiveRule(ReadingType readingType) {
        return device.getDeviceConfiguration().getValidationRuleSets().stream()
                .flatMap(s -> s.getRules().stream())
                .filter(ValidationRule::isActive)
                .anyMatch(r -> r.getReadingTypes().contains(readingType));
    }

    private boolean hasSameRule(Channel channel) {
        if (channel.getCalculatedReadingType(clock.instant()).isPresent()) {
            return hasSameRule(channel.getReadingType()) || hasSameRule(channel.getCalculatedReadingType(clock.instant()).get());
        } else {
            return hasSameRule(channel.getReadingType());
        }
    }

    private boolean hasSameRule(Register<?, ?> register) {
        if (register.getCalculatedReadingType(clock.instant()).isPresent()) {
            return hasSameRule(register.getReadingType()) || hasSameRule(register.getCalculatedReadingType(clock.instant()).get());
        } else {
            return hasSameRule(register.getReadingType());
        }
    }

    private boolean hasSameRule(ReadingType readingType) {
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
        return meter.getMeterActivations().stream()
                .sorted(MOST_RECENT_FIRST)
                .map(MeterActivation::getChannelsContainer)
                .map(channelsContainer -> validationService.getLastChecked(channelsContainer).filter(lastChecked -> lastChecked.isAfter(channelsContainer.getStart()))) // may be use evaluator to allow caching this
                .flatMap(asStream())
                .findFirst();
    }

    private Optional<Instant> getLastValidationRun(Meter meter) {
        return meter.getMeterActivations().stream()
                .sorted(MOST_RECENT_FIRST)
                .map(MeterActivation::getChannelsContainer)
                .map(validationService::getLastValidationRun)  // may be use evaluator to allow caching this
                .flatMap(asStream())
                .findFirst();
    }

    private Optional<Instant> getLastChecked(ReadingType readingType) {
        return getEvaluator().getLastChecked(fetchKoreMeter(), readingType);
    }

    private void validate(ReadingType readingType) {
        fetchKoreMeter().getChannelsContainers()
                .forEach(channelContainer -> validationService.validate(EnumSet.of(QualityCodeSystem.MDC), channelContainer, readingType));
    }

    private Meter fetchKoreMeter() {
        if (meter == null) {
            if (device.getMeterReference().isPresent()) {
                meter = device.getMeterReference().get();
            } else {
                throw new UnsupportedOperationException("No Kore Meter for device " + device.getId());
            }
        }
        return meter;
    }

    private ValidationEvaluator getEvaluator() {
        if (evaluator == null) {
            evaluator = validationService.getEvaluator(fetchKoreMeter());
        }
        return evaluator;
    }

    @Override
    public List<ChannelValidationRuleOverriddenPropertiesImpl> findAllOverriddenProperties() {
        return mapper().find(ValidationEstimationRuleOverriddenPropertiesImpl.Fields.DEVICE.fieldName(), this.device);
    }

    @Override
    public Optional<ChannelValidationRuleOverriddenPropertiesImpl> findAndLockChannelValidationRuleOverriddenProperties(long id, long version) {
        return mapper().lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<ChannelValidationRuleOverriddenPropertiesImpl> findChannelValidationRuleOverriddenProperties(long id) {
        return mapper().getOptional(id);
    }

    @Override
    public Optional<ChannelValidationRuleOverriddenPropertiesImpl> findOverriddenProperties(ValidationRule validationRule, ReadingType readingType) {
        String[] fieldNames = {
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.DEVICE.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.READINGTYPE.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_NAME.fieldName(),
                ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_IMPL.fieldName(),
                ChannelValidationRuleOverriddenPropertiesImpl.Fields.VALIDATION_ACTION.fieldName()
        };
        Object[] values = {
                this.device,
                readingType,
                validationRule.getName(),
                validationRule.getImplementation(),
                validationRule.getAction()
        };
        return mapper().getUnique(fieldNames, values);
    }

    private DataMapper<ChannelValidationRuleOverriddenPropertiesImpl> mapper() {
        return dataModel.mapper(ChannelValidationRuleOverriddenPropertiesImpl.class);
    }

    @Override
    public PropertyOverrider overridePropertiesFor(ValidationRule validationRule, ReadingType readingType) {
        return new PropertyOverriderImpl(validationRule, readingType);
    }

    class PropertyOverriderImpl implements PropertyOverrider {

        private final ValidationRule validationRule;
        private final ReadingType readingType;

        private final Map<String, Object> properties = new HashMap<>();

        PropertyOverriderImpl(ValidationRule validationRule, ReadingType readingType) {
            this.validationRule = validationRule;
            this.readingType = readingType;
        }

        @Override
        public PropertyOverrider override(String propertyName, Object propertyValue) {
            properties.put(propertyName, propertyValue);
            return this;
        }

        @Override
        public ChannelValidationRuleOverriddenProperties complete() {
            ChannelValidationRuleOverriddenPropertiesImpl overriddenProperties = createChannelValidationRuleOverriddenProperties();
            overriddenProperties.setProperties(this.properties);
            overriddenProperties.validate();
            if (this.properties.isEmpty()) {
                return null;// we don't want to persist entity without overridden properties
            }
            overriddenProperties.save();
            return overriddenProperties;
        }

        private ChannelValidationRuleOverriddenPropertiesImpl createChannelValidationRuleOverriddenProperties() {
            return dataModel.getInstance(ChannelValidationRuleOverriddenPropertiesImpl.class)
                    .init(device, readingType, validationRule.getName(), validationRule.getImplementation(), validationRule.getAction());
        }
    }
}
