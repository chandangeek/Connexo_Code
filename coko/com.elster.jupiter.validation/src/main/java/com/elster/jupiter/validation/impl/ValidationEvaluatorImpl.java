package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
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
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
* Created by tgr on 5/09/2014.
*/
class ValidationEvaluatorImpl implements ValidationEvaluator {

    private final ValidationServiceImpl validationService;

    ValidationEvaluatorImpl(ValidationServiceImpl validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean isAllDataValidated(MeterActivation meterActivation) {
        for (IMeterActivationValidation meterActivationValidation : validationService.getIMeterActivationValidations(meterActivation)) {
            if (!meterActivationValidation.isAllDataValidated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(CimChannel channel, List<? extends BaseReading> readings) {
        List<DataValidationStatus> result = new ArrayList<>(readings.size());
        if (!readings.isEmpty()) {
            List<? extends IChannelValidation> channelValidations = validationService.getChannelValidations(channel.getChannel());
            boolean configured = !channelValidations.isEmpty();
            Instant lastChecked = configured ? getMinLastChecked(channelValidations.stream()
                    .filter(IChannelValidation::hasActiveRules)
                    .map(IChannelValidation::getLastChecked).collect(Collectors.toSet())) : null;

            ListMultimap<String, IValidationRule> validationRuleMap = getValidationRulesPerReadingQuality(channelValidations);

            ListMultimap<Instant, ReadingQualityRecord> readingQualities = getReadingQualities(channel, getInterval(readings));
            ReadingQualityType validatedAndOk = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> qualities = (readingQualities.containsKey(reading.getTimeStamp()) ? new ArrayList<>(readingQualities.get(reading.getTimeStamp())) : new ArrayList<ReadingQualityRecord>());
                if (configured) {
                    if (wasValidated(lastChecked, reading.getTimeStamp())) {
                        qualities.add(channel.createReadingQuality(validatedAndOk, reading.getTimeStamp()));
                    }
                }
                boolean fullyValidated = false;
                if (configured) {
                    fullyValidated = (wasValidated(lastChecked, reading.getTimeStamp()));
                }
                result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities, validationRuleMap));

            }
        }

        return result;
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(CimChannel channel, List<? extends BaseReading> readings, Range<Instant> interval) {
        List<DataValidationStatus> result = new ArrayList<>();
        List<? extends IChannelValidation> channelValidations = validationService.getChannelValidations(channel.getChannel());
        boolean configured = !channelValidations.isEmpty();
        Instant lastChecked = configured ? getMinLastChecked(channelValidations.stream()
                .filter(IChannelValidation::hasActiveRules)
                .map(IChannelValidation::getLastChecked).collect(Collectors.toSet())) : null;

        ListMultimap<String, IValidationRule> validationRuleMap = getValidationRulesPerReadingQuality(channelValidations);

        ListMultimap<Instant, ReadingQualityRecord> readingQualities = getReadingQualities(channel, interval);

        Set<Instant> timesWithReadings = new HashSet<>();

        ReadingQualityType validatedAndOk = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);
        for (BaseReading reading : readings) {
            boolean containsKey = readingQualities.containsKey(reading.getTimeStamp());
            List<ReadingQualityRecord> qualities = (containsKey ? new ArrayList<>(readingQualities.get(reading.getTimeStamp())) : new ArrayList<>());
            timesWithReadings.add(reading.getTimeStamp());
            if (configured) {
                if (wasValidated(lastChecked, reading.getTimeStamp())) {
                    qualities.add(channel.createReadingQuality(validatedAndOk, reading.getTimeStamp()));
                }
            }
            boolean fullyValidated = false;
            if (configured) {
                fullyValidated = (wasValidated(lastChecked, reading.getTimeStamp()));
            }
            result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities, validationRuleMap));

        }

        Set<Instant> timesWithoutReadings = new HashSet<>(readingQualities.keySet());
        timesWithoutReadings.removeAll(timesWithReadings);

        for (Instant readingTimestamp : timesWithoutReadings) {
            List<ReadingQuality> qualities = new ArrayList<>(readingQualities.get(readingTimestamp));
            result.add(createDataValidationStatusListFor(readingTimestamp, false, qualities, validationRuleMap));
        }
        result.sort(Comparator.comparing(DataValidationStatus::getReadingTimestamp));
        return result;
    }


    private Instant getMinLastChecked(Iterable<Instant> dates) {
        Comparator<Instant> comparator = nullsFirst(naturalOrder());
        return dates.iterator().hasNext() ? Ordering.from(comparator).min(dates) : null;
    }

    private ListMultimap<String, IValidationRule> getValidationRulesPerReadingQuality(List<? extends IChannelValidation> channelValidations) {
        Query<IValidationRule> ruleQuery = validationService.getAllValidationRuleQuery();
        Set<IValidationRule> rules = channelValidations.stream()
                .map(IChannelValidation::getMeterActivationValidation)
                .map(IMeterActivationValidation::getRuleSet)                
                .flatMap(ruleSet -> ruleQuery.select(Where.where("ruleSet").isEqualTo(ruleSet)).stream())
                .collect(Collectors.toSet());
        return Multimaps.index(rules, i -> i.getReadingQualityType().getCode());
    }

    private ListMultimap<Instant, ReadingQualityRecord> getReadingQualities(CimChannel channel, Range<Instant> interval) {
        List<ReadingQualityRecord> readingQualities = channel.findReadingQuality(interval);
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

    private DataValidationStatus createDataValidationStatusListFor(Instant timeStamp, boolean completelyValidated, List<? extends ReadingQuality> qualities, ListMultimap<String, IValidationRule> validationRuleMap) {
        DataValidationStatusImpl validationStatus = new DataValidationStatusImpl(timeStamp, completelyValidated);
        for (ReadingQuality quality : qualities) {
            validationStatus.addReadingQuality(quality, filterDuplicates(validationRuleMap.get(quality.getTypeCode())));
        }
        return validationStatus;
    }

    private List<IValidationRule> filterDuplicates(List<IValidationRule> iValidationRules) {
        Map<String, IValidationRule> filter = new HashMap<>();
        for (IValidationRule iValidationRule : iValidationRules) {
            if (filter.containsKey(iValidationRule.getImplementation())) {
                if (iValidationRule.getObsoleteDate() != null) {
                    filter.put(iValidationRule.getImplementation(), iValidationRule);
                }
            } else {
                filter.put(iValidationRule.getImplementation(), iValidationRule);
            }
        }
        return new ArrayList<>(filter.values());
    }

    @Override
    public boolean isValidationEnabled(Meter meter) {
        return validationService.validationEnabled(meter);
    }

    @Override
    public boolean isValidationEnabled(Channel channel) {
        return validationService.getMeterActivationValidations(channel.getMeterActivation()).stream()
                .map(m -> m.getChannelValidation(channel))
                .flatMap(asStream())
                .anyMatch(IChannelValidation::hasActiveRules);

    }

    @Override
    public Optional<Instant> getLastChecked(Meter meter, ReadingType readingType) {
        return meter.getMeterActivations().stream()
                .flatMap(m -> m.getChannels().stream())
                .filter(k -> k.getReadingTypes().contains(readingType))
                .filter(validationService::isValidationActive)
                .map(validationService::getLastChecked)
                .flatMap(asStream())
                .max(naturalOrder());
    }
}
