package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.Comparator.naturalOrder;

/**
 * Created by tgr on 5/09/2014.
 */
class ValidationEvaluatorForMeter implements ValidationEvaluator {

    private final Meter meter;
    private final ValidationServiceImpl validationService;

    private Map<Long, MeterActivationValidationContainer> mapToValidation;   
    private Map<Long, ChannelValidationContainer> mapChannelToValidation;
    private Multimap<String, IValidationRule> mapQualityToRule;

    private Optional<Boolean> isEnabled = Optional.empty();

    ValidationEvaluatorForMeter(ValidationServiceImpl validationService, Meter meter, Range<Instant> interval) {
        this.validationService = validationService;
        this.meter = meter;
    }


    @Override
    public boolean isAllDataValidated(MeterActivation meterActivation) {
        return getMapToValidation().get(meterActivation.getId()).isAllDataValidated();               
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(CimChannel channel, List<? extends BaseReading> readings) {
        return getValidationStatus(channel, readings, getInterval(readings));
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(CimChannel channel, List<? extends BaseReading> readings, Range<Instant> interval) {
        List<DataValidationStatus> result = new ArrayList<>();
        ChannelValidationContainer channelValidations = getMapChannelToValidation().get(channel.getChannel().getId());
        boolean configured = !channelValidations.isEmpty();
        Instant lastChecked = channelValidations.getLastChecked().orElse(null);                
        ListMultimap<Instant, ReadingQualityRecord> readingQualities = getActualReadingQualities(channel, interval);
        Set<Instant> timesWithReadings = new HashSet<>();
        ReadingQualityType validatedAndOk = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);
        for (BaseReading reading : readings) {
            boolean containsKey = readingQualities.containsKey(reading.getTimeStamp());
            List<ReadingQualityRecord> qualities = (containsKey ? new ArrayList<>(readingQualities.get(reading.getTimeStamp())) : new ArrayList<ReadingQualityRecord>());
            timesWithReadings.add(reading.getTimeStamp());
            if (configured && wasValidated(lastChecked, reading.getTimeStamp())) {
                qualities.add(channel.createReadingQuality(validatedAndOk, reading.getTimeStamp()));
            }
            boolean fullyValidated = false;
            if (configured) {
                fullyValidated = (wasValidated(lastChecked, reading.getTimeStamp()));
            }
            result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities));
        }

        Set<Instant> timesWithoutReadings = new HashSet<>(readingQualities.keySet());
        timesWithoutReadings.removeAll(timesWithReadings);

        timesWithoutReadings.forEach(readingTimestamp -> {
            List<ReadingQuality> qualities = new ArrayList<>(readingQualities.get(readingTimestamp));
            boolean wasValidated = wasValidated(lastChecked, readingTimestamp);
            boolean fullyValidated = configured && wasValidated;
            result.add(createDataValidationStatusListFor(readingTimestamp, fullyValidated, qualities));
        });
        result.sort(Comparator.comparing(DataValidationStatus::getReadingTimestamp));
        return result;
    }

    @Override
    public boolean isValidationEnabled(Meter meter) {
        if (this.meter != meter) {
            return validationService.getEvaluator(meter, Range.atLeast(Instant.EPOCH)).isValidationEnabled(meter);
        }
        return isEnabled.orElseGet(() -> {
            isEnabled = Optional.of(validationService.validationEnabled(meter));
            return isEnabled.get();
        });
    }

    @Override
    public boolean isValidationEnabled(Channel channel) {
        ChannelValidationContainer channelValidations = getMapChannelToValidation().get(channel.getId());
        return channelValidations != null && channelValidations.isValidationActive();
    }

    @Override
    public Optional<Instant> getLastChecked(Meter meter, ReadingType readingType) {    
        return meter.getMeterActivations().stream()
                .flatMap(m -> m.getChannels().stream())
                .filter(c -> c.getReadingTypes().contains(readingType))
                .map(channel -> getMapChannelToValidation().get(channel.getId()))
                .filter(Objects::nonNull)
                .map(ChannelValidationContainer::getLastChecked)
                .flatMap(asStream())
                .max(naturalOrder());
    }

    private ImmutableMap<Long, MeterActivationValidationContainer> initMeterActivationMap(ValidationServiceImpl validationService, Meter meter) {
        ImmutableMap.Builder<Long, MeterActivationValidationContainer> validationMapBuilder = ImmutableMap.builder();
        for (MeterActivation meterActivation : meter.getMeterActivations()) {
            MeterActivationValidationContainer container = ((ValidationServiceImpl) validationService).updatedMeterActivationValidationsFor(meterActivation);
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

    private ListMultimap<Instant, ReadingQualityRecord> getActualReadingQualities(CimChannel channel, Range<Instant> interval) {
        List<ReadingQualityRecord> readingQualities = channel.findActualReadingQuality(interval);
        return Multimaps.index(readingQualities, ReadingQualityRecord::getReadingTimestamp);
    }

    private Range<Instant> getInterval(List<? extends BaseReading> readings) {
        Instant min = null;
        Instant max = null;
        for (BaseReading reading : readings) {
            if (min == null || reading.getTimeStamp().isBefore(min)) {
                min = reading.getTimeStamp();
            }
            if (max == null || reading.getTimeStamp().isAfter(max)) {
                max = reading.getTimeStamp();
            }
        }
        return Ranges.closed(min, max);
    }

    private boolean wasValidated(Instant lastChecked, Instant readingTimestamp) {
        return lastChecked != null && readingTimestamp.compareTo(lastChecked) <= 0;
    }

    private DataValidationStatus createDataValidationStatusListFor(Instant timeStamp, boolean completelyValidated, List<? extends ReadingQuality> qualities) {
        DataValidationStatusImpl validationStatus = new DataValidationStatusImpl(timeStamp, completelyValidated);
        for (ReadingQuality quality : qualities) {
            validationStatus.addReadingQuality(quality, filterDuplicates(getMapQualityToRule().get(quality.getTypeCode())));
        }
        return validationStatus;
    }

    private List<IValidationRule> filterDuplicates(Collection<IValidationRule> iValidationRules) {
        Map<String, IValidationRule> collect = iValidationRules.stream()
                .collect(Collectors.toMap(IValidationRule::getImplementation, Function.<IValidationRule>identity(), (a, b) -> a.isObsolete() ? b : a));
        return new ArrayList<>(collect.values());
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

    private Multimap<String, IValidationRule> getMapQualityToRule() {
        if (mapQualityToRule == null) {
            mapQualityToRule = initRulesPerQuality();
        }
        return mapQualityToRule;
    }
}
