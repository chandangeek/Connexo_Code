package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 1/04/2015
 * Time: 11:52
 */
public abstract class AbstractValidationEvaluator implements ValidationEvaluator {
    @Override
    public List<DataValidationStatus> getValidationStatus(CimChannel channel, List<? extends BaseReading> readings) {
        return getValidationStatus(channel, readings, getInterval(readings));
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(CimChannel channel, List<? extends BaseReading> readings, Range<Instant> interval) {
        List<DataValidationStatus> result = new ArrayList<>();
        ChannelValidationContainer channelValidations = getChannelValidationContainer(channel.getChannel());
        boolean configured = !channelValidations.isEmpty();

        Instant lastChecked = channelValidations.getLastChecked().orElse(null);

        Multimap<String, IValidationRule> validationRuleMap = getMapQualityToRule(channelValidations);

        ListMultimap<Instant, ReadingQualityRecord> readingQualities = getActualReadingQualities(channel, interval);

        Set<Instant> timesWithReadings = new HashSet<>();

        ReadingQualityType validatedAndOk = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);
        for (BaseReading reading : readings) {
            boolean containsKey = readingQualities.containsKey(reading.getTimeStamp());
            List<ReadingQualityRecord> qualities = (containsKey ? new ArrayList<>(readingQualities.get(reading.getTimeStamp())) : new ArrayList<>());
            timesWithReadings.add(reading.getTimeStamp());
            if (configured && wasValidated(lastChecked, reading.getTimeStamp()) && qualities.stream().noneMatch(ReadingQualityRecord::isSuspect)) {
                qualities.add(channel.createReadingQuality(validatedAndOk, reading.getTimeStamp()));
            }
            boolean fullyValidated = false;
            if (configured) {
                fullyValidated = (wasValidated(lastChecked, reading.getTimeStamp()));
            }
            result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities, validationRuleMap));
        }

        Set<Instant> timesWithoutReadings = new HashSet<>(readingQualities.keySet());
        timesWithoutReadings.removeAll(timesWithReadings);

        timesWithoutReadings.forEach(readingTimestamp -> {
            List<ReadingQuality> qualities = new ArrayList<>(readingQualities.get(readingTimestamp));
            boolean wasValidated = wasValidated(lastChecked, readingTimestamp);
            boolean fullyValidated = configured && wasValidated;
            result.add(createDataValidationStatusListFor(readingTimestamp, fullyValidated, qualities, validationRuleMap));
        });
//        result.sort(Comparator.comparing(DataValidationStatus::getReadingTimestamp));
        return result;
    }

    protected ListMultimap<Instant, ReadingQualityRecord> getActualReadingQualities(CimChannel channel, Range<Instant> interval) {
        List<ReadingQualityRecord> readingQualities = channel.findActualReadingQuality(interval);
        return Multimaps.index(readingQualities, ReadingQualityRecord::getReadingTimestamp);
    }

    Range<Instant> getInterval(List<? extends BaseReading> readings) {
        return readings.stream()
                .map(BaseReading::getTimeStamp)
                .map(Range::singleton)
                .reduce(Range::span)
                .orElse(null);
    }

    List<IValidationRule> filterDuplicates(Collection<IValidationRule> iValidationRules) {
        Map<String, IValidationRule> collect = iValidationRules.stream()
                .collect(Collectors.toMap(IValidationRule::getImplementation, Function.<IValidationRule>identity(), (a, b) -> a.isObsolete() ? b : a));
        return new ArrayList<>(collect.values());
    }

    DataValidationStatus createDataValidationStatusListFor(Instant timeStamp, boolean completelyValidated, List<? extends ReadingQuality> qualities, Multimap<String, IValidationRule> validationRuleMap) {
        DataValidationStatusImpl validationStatus = new DataValidationStatusImpl(timeStamp, completelyValidated);
        for (ReadingQuality quality : qualities) {
            validationStatus.addReadingQuality(quality, filterDuplicates(validationRuleMap.get(quality.getTypeCode())));
        }
        return validationStatus;
    }

    abstract ChannelValidationContainer getChannelValidationContainer(Channel channel);

    abstract Multimap<String, IValidationRule> getMapQualityToRule(ChannelValidationContainer channelValidations);

    boolean wasValidated(Instant lastChecked, Instant readingTimestamp) {
        return lastChecked != null && readingTimestamp.compareTo(lastChecked) <= 0;
    }
}
