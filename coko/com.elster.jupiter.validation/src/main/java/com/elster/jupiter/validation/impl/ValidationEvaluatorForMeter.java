package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.notNull;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * Created by tgr on 5/09/2014.
 */
class ValidationEvaluatorForMeter implements ValidationEvaluator {

    private static final ReadingQuality OK_QUALITY = new ReadingQuality() {
        @Override
        public String getComment() {
            return "";
        }

        @Override
        public String getTypeCode() {
            return ReadingQualityType.MDM_VALIDATED_OK_CODE;
        }
    };

    private final Meter meter;
    private final Range<Instant> interval;
    private final ValidationServiceImpl validationService;

    private Multimap<Long, IMeterActivationValidation> mapToValidation;
    private Multimap<Instant, ReadingQualityRecord> readingQualities;
    private Multimap<Long, ChannelValidation> mapChannelToValidation;
    private Multimap<String, IValidationRule> mapQualityToRule;

    private Optional<Boolean> isEnabled = Optional.empty();

    ValidationEvaluatorForMeter(ValidationServiceImpl validationService, Meter meter, Range<Instant> interval) {
        this.validationService = validationService;
        this.meter = meter;
        this.interval = interval;
    }

    @Override
    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
       return ValidationResult.getValidationResult(qualities);
    }

    @Override
    public boolean isAllDataValidated(MeterActivation meterActivation) {
        return getMapToValidation().get(meterActivation.getId()).stream()
                .allMatch(IMeterActivationValidation::isAllDataValidated);
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings) {
        return getValidationStatus(channel, readings, getInterval(readings));
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Range<Instant> interval) {
        List<DataValidationStatus> result = new ArrayList<>();
        Collection<ChannelValidation> channelValidations = getMapChannelToValidation().get(channel.getId());
        boolean configured = !channelValidations.isEmpty();
        Instant lastChecked = configured ? getMinLastChecked(channelValidations.stream()
                .filter(ChannelValidation::hasActiveRules)
                .map(ChannelValidation::getLastChecked).collect(Collectors.toSet())) : null;

        ListMultimap<Instant, ReadingQualityRecord> readingQualities = getActualReadingQualities(channel, interval);

        Set<Instant> timesWithReadings = new HashSet<>();

        ReadingQualityType validatedAndOk = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);
        for (BaseReading reading : readings) {
            boolean containsKey = readingQualities.containsKey(reading.getTimeStamp());
            List<ReadingQualityRecord> qualities = (containsKey ? readingQualities.get(reading.getTimeStamp()) : new ArrayList<ReadingQualityRecord>());
            timesWithReadings.add(reading.getTimeStamp());
            if (qualities.isEmpty() && configured && wasValidated(lastChecked, reading.getTimeStamp())) {
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
        Collection<ChannelValidation> channelValidations = getMapChannelToValidation().get(channel.getId());
        return channelValidations != null && channelValidations.stream().anyMatch(ChannelValidation::hasActiveRules);
    }

    @Override
    public Optional<Instant> getLastChecked(Meter meter, ReadingType readingType) {
        return meter.getMeterActivations().stream()
                .flatMap(m -> m.getChannels().stream())
                .filter(c -> c.getReadingTypes().contains(readingType))
                .map(c -> getMapChannelToValidation().get(c.getId()))
                .filter(notNull())
                .flatMap(Collection::stream)
                .map(ChannelValidation::getLastChecked)
                .filter(notNull())
                .max(naturalOrder());
    }

    private ImmutableMultimap<Instant, ReadingQualityRecord> initReadingQualitiesMap(Meter meter, Range<Instant> interval) {
        ImmutableMultimap.Builder<Instant, ReadingQualityRecord> readingQualityMapBuilder = ImmutableMultimap.builder();
        meter.getMeterActivations().stream()
                .flatMap(m -> m.getChannels().stream())
                .flatMap(c -> c.findReadingQuality(interval).stream())
                .forEach(r -> {
                    readingQualityMapBuilder.put(r.getTimestamp(), r);
                });
        return readingQualityMapBuilder.build();
    }

    private ImmutableMultimap<Long, IMeterActivationValidation> initMeterActivationMap(ValidationServiceImpl validationService, Meter meter) {
        ImmutableMultimap.Builder<Long, IMeterActivationValidation> validationMapBuilder = ImmutableMultimap.builder();
        for (MeterActivation meterActivation : meter.getMeterActivations()) {
            List<IMeterActivationValidation> meterActivationValidations = ((ValidationServiceImpl) validationService).getUpdatedMeterActivationValidations(meterActivation);
            validationMapBuilder.putAll(meterActivation.getId(), meterActivationValidations);
        }
        return validationMapBuilder.build();
    }

    private ImmutableMultimap<Long, ChannelValidation> initChannelMap(Meter meter) {
        ImmutableMultimap.Builder<Long, ChannelValidation> channelValidationMapBuilder = ImmutableMultimap.builder();
        meter.getMeterActivations().stream()
                .flatMap(m -> m.getChannels().stream())
                .forEach(c -> {
                    getMapToValidation().get(c.getMeterActivation().getId()).stream()
                            .flatMap(m -> m.getChannelValidations().stream())
                            .filter(cv -> cv.getChannel().getId() == c.getId())
                            .forEach(cv -> channelValidationMapBuilder.put(c.getId(), cv));
                });
        return channelValidationMapBuilder.build();
    }

    private ImmutableListMultimap<String, IValidationRule> initRulesPerQuality() {
        Query<IValidationRule> ruleQuery = this.validationService.getAllValidationRuleQuery();
        Set<IValidationRule> rules = getMapChannelToValidation().values().stream()
                .map(ChannelValidation::getMeterActivationValidation)
                .map(MeterActivationValidation::getRuleSet)
                .map(ValidationRuleSet::getId)
                .map(id -> ruleQuery.select(Operator.EQUAL.compare("ruleSetId", id)))
                .flatMap(l -> l.stream())
                .collect(Collectors.toSet());
        return Multimaps.index(rules, i -> i.getReadingQualityType().getCode());
    }


    private Instant getMinLastChecked(Iterable<Instant> dates) {
        Comparator<Instant> comparator = nullsFirst(naturalOrder());
        return dates.iterator().hasNext() ? Ordering.from(comparator).min(dates) : null;
    }


    private ListMultimap<Instant, ReadingQualityRecord> getActualReadingQualities(Channel channel, Range<Instant> interval) {
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

    private Multimap<Instant, ReadingQualityRecord> getReadingQualities() {
        if (readingQualities == null) {
            readingQualities = initReadingQualitiesMap(meter, interval);
        }
        return readingQualities;
    }

    private Multimap<Long, IMeterActivationValidation> getMapToValidation() {
        if (mapToValidation == null) {
            mapToValidation = initMeterActivationMap(validationService, meter);
        }
        return mapToValidation;
    }

    private Multimap<Long, ChannelValidation> getMapChannelToValidation() {
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
