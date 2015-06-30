package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.Comparator.naturalOrder;

/**
 * Created by tgr on 5/09/2014.
 */
class ValidationEvaluatorForMeter extends AbstractValidationEvaluator {

    private final Meter meter;
    private final ValidationServiceImpl validationService;

    private Map<Long, MeterActivationValidationContainer> mapToValidation;   
    private Map<Long, ChannelValidationContainer> mapChannelToValidation;
    private Multimap<String, IValidationRule> mapQualityToRule;

    private Optional<Boolean> isEnabled = Optional.empty();
    private Optional<Boolean> isOnStorageEnabled = Optional.empty();

    ValidationEvaluatorForMeter(ValidationServiceImpl validationService, Meter meter, Range<Instant> interval) {
        this.validationService = validationService;
        this.meter = meter;
    }


    @Override
    public boolean isAllDataValidated(MeterActivation meterActivation) {
        return getMapToValidation().get(meterActivation.getId()).isAllDataValidated();               
    }

    @Override
    public boolean isValidationEnabled(Meter meter) {
        return isEnabled.orElseGet(() -> {
            isEnabled = Optional.of(validationService.validationEnabled(meter));
            return isEnabled.get();
        });
    }

    @Override
    public boolean isValidationOnStorageEnabled(Meter meter) {
        return isOnStorageEnabled.orElseGet(() -> {
            isOnStorageEnabled = Optional.of(validationService.validationOnStorageEnabled(meter));
            return isOnStorageEnabled.get();
        });
    }

    @Override
    public boolean isValidationEnabled(Channel channel) {
        ChannelValidationContainer channelValidations = getMapChannelToValidation().get(channel.getId());
        return channelValidations != null && channelValidations.isValidationActive();
    }

    @Override
    public Optional<Instant> getLastChecked(ReadingContainer readingContainer, ReadingType readingType) {
        return readingContainer.getMeterActivations().stream()
                .flatMap(m -> m.getChannels().stream())
                .filter(k -> k.getReadingTypes().contains(readingType))
                .map(channel -> getMapChannelToValidation().get(channel.getId()))
                .filter(Objects::nonNull)
                .map(ChannelValidationContainer::getLastChecked)
                .flatMap(asStream())
                .max(naturalOrder());
    }

    @Override
    ChannelValidationContainer getChannelValidationContainer(Channel channel) {
        return getMapChannelToValidation().get(channel.getId());
    }

    @Override
    Multimap<String, IValidationRule> getMapQualityToRule(ChannelValidationContainer channelValidations) {
        if (mapQualityToRule == null) {
            mapQualityToRule = initRulesPerQuality();
        }
        return mapQualityToRule;
    }

    private ImmutableMap<Long, MeterActivationValidationContainer> initMeterActivationMap(ValidationServiceImpl validationService, Meter meter) {
        ImmutableMap.Builder<Long, MeterActivationValidationContainer> validationMapBuilder = ImmutableMap.builder();
        for (MeterActivation meterActivation : meter.getMeterActivations()) {
            MeterActivationValidationContainer container = validationService.updatedMeterActivationValidationsFor(meterActivation);
            validationMapBuilder.put(meterActivation.getId(), container);
        }
        return validationMapBuilder.build();
    }

    private Map<Long, ChannelValidationContainer> initChannelMap(Meter meter) {
        ImmutableMap.Builder<Long, ChannelValidationContainer> channelValidationMapBuilder = ImmutableMap.builder();
        meter.getMeterActivations().stream()
                .flatMap(m -> m.getChannels().stream())             
                .forEach(c -> channelValidationMapBuilder.put(c.getId(), getMapToValidation().get(c.getMeterActivation().getId()).channelValidationsFor(c)));                                           
        return channelValidationMapBuilder.build();
    }

    private ImmutableListMultimap<String, IValidationRule> initRulesPerQuality() {
        Set<IValidationRule> rules = getMapToValidation().values().stream()
                .flatMap(meterActivationValidationContainer -> meterActivationValidationContainer.ruleSets().stream())
                .distinct()
                .flatMap(ruleSet -> ruleSet.getRules().stream())
                .map(IValidationRule.class::cast)
                .collect(Collectors.toSet());
        return Multimaps.index(rules, i -> i.getReadingQualityType().getCode());
    }

    private Map<Long, MeterActivationValidationContainer> getMapToValidation() {
        if (mapToValidation == null) {
            mapToValidation = initMeterActivationMap(validationService, meter);
        }
        return mapToValidation;
    }

    private Map<Long, ChannelValidationContainer> getMapChannelToValidation() {
        if (mapChannelToValidation == null) {
            mapChannelToValidation = initChannelMap(meter);
        }
        return mapChannelToValidation;
    }
}
