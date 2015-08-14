package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.*;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 1/04/2015
 * Time: 11:52
 */
public abstract class AbstractValidationEvaluator implements ValidationEvaluator {

    public boolean isAllDataValid(MeterActivation meterActivation){
        ReadingQualityType suspect = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT);
        return meterActivation.getChannels().stream()
                .flatMap(channel -> channel.findActualReadingQuality(suspect, meterActivation.getRange()).stream())
                .count() == 0;
    }
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
            result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities, Collections.<ReadingQualityRecord>emptyList(), validationRuleMap, null));
        }

        Set<Instant> timesWithoutReadings = new HashSet<>(readingQualities.keySet());
        timesWithoutReadings.removeAll(timesWithReadings);

        timesWithoutReadings.forEach(readingTimestamp -> {
            List<ReadingQualityRecord> qualities = new ArrayList<>(readingQualities.get(readingTimestamp));
            boolean wasValidated = wasValidated(lastChecked, readingTimestamp);
            boolean fullyValidated = configured && wasValidated;
            if (configured && wasValidated && qualities.stream().noneMatch(ReadingQualityRecord::isSuspect)) {
                qualities.add(channel.createReadingQuality(validatedAndOk, readingTimestamp));
            }
            result.add(createDataValidationStatusListFor(readingTimestamp, fullyValidated, qualities, Collections.<ReadingQualityRecord>emptyList(), validationRuleMap, null));
        });
//        result.sort(Comparator.comparing(DataValidationStatus::getReadingTimestamp));
        return result;
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(CimChannel mainChannel, CimChannel bulkChannel, List<? extends BaseReading> readings) {
        return getValidationStatus(mainChannel, bulkChannel, readings, getInterval(readings));
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(CimChannel mainChannel, CimChannel bulkChannel, List<? extends BaseReading> readings, Range<Instant> interval) {
        List<DataValidationStatus> result = new ArrayList<>();
        ChannelValidationContainer mainChannelValidations = getChannelValidationContainer(mainChannel.getChannel());
        ChannelValidationContainer bulkChannelValidations = getChannelValidationContainer(bulkChannel.getChannel());
        boolean configured = !mainChannelValidations.isEmpty();

        Instant lastChecked = mainChannelValidations.getLastChecked().orElse(null);

        Multimap<String, IValidationRule> validationRuleMap = getMapQualityToRule(mainChannelValidations);
        Multimap<String, IValidationRule> bulkValidationRuleMap = getMapQualityToRule(bulkChannelValidations);

        ListMultimap<Instant, ReadingQualityRecord> readingQualities = getActualReadingQualities(mainChannel, interval);
        ListMultimap<Instant, ReadingQualityRecord> bulkReadingQualities = getActualReadingQualities(bulkChannel, interval);

        Set<Instant> timesWithReadings = new HashSet<>();

        ReadingQualityType validatedAndOk = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);
        for (BaseReading reading : readings) {
            boolean containsKey = readingQualities.containsKey(reading.getTimeStamp());
            List<ReadingQualityRecord> qualities = (containsKey ? new ArrayList<>(readingQualities.get(reading.getTimeStamp())) : new ArrayList<>());
            boolean bulkContainsKey = bulkReadingQualities.containsKey(reading.getTimeStamp());
            List<ReadingQualityRecord> bulkQualities = (bulkContainsKey ? new ArrayList<>(bulkReadingQualities.get(reading.getTimeStamp())) : new ArrayList<>());
            timesWithReadings.add(reading.getTimeStamp());
            if (configured && wasValidated(lastChecked, reading.getTimeStamp())) {
                if (qualities.stream().noneMatch(ReadingQualityRecord::isSuspect)) {
                    qualities.add(mainChannel.createReadingQuality(validatedAndOk, reading.getTimeStamp()));
                }
                if (bulkQualities.stream().noneMatch(ReadingQualityRecord::isSuspect)) {
                    bulkQualities.add(bulkChannel.createReadingQuality(validatedAndOk, reading.getTimeStamp()));
                }
            }
            boolean fullyValidated = false;
            if (configured) {
                fullyValidated = (wasValidated(lastChecked, reading.getTimeStamp()));
            }
            result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities, bulkQualities, validationRuleMap, bulkValidationRuleMap));
        }

        Set<Instant> timesWithoutReadings = new HashSet<>(readingQualities.keySet());
        timesWithoutReadings.removeAll(timesWithReadings);

        timesWithoutReadings.forEach(readingTimestamp -> {
            List<ReadingQuality> qualities = new ArrayList<>(readingQualities.get(readingTimestamp));
            List<ReadingQuality> bulkQualities = new ArrayList<>(bulkReadingQualities.get(readingTimestamp));
            boolean wasValidated = wasValidated(lastChecked, readingTimestamp);
            boolean fullyValidated = configured && wasValidated;
            result.add(createDataValidationStatusListFor(readingTimestamp, fullyValidated, qualities, bulkQualities, validationRuleMap, bulkValidationRuleMap));
        });
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

    DataValidationStatus createDataValidationStatusListFor(Instant timeStamp, boolean completelyValidated, List<? extends ReadingQuality> qualities, List<? extends ReadingQuality> bulkQualities,
                                                           Multimap<String, IValidationRule> validationRuleMap, Multimap<String, IValidationRule> bulkValidationRuleMap) {
        DataValidationStatusImpl validationStatus = new DataValidationStatusImpl(timeStamp, completelyValidated);
        for (ReadingQuality quality : qualities) {
            validationStatus.addReadingQuality(quality, filterDuplicates(validationRuleMap.get(quality.getTypeCode())));
        }
        for (ReadingQuality quality : bulkQualities) {
            validationStatus.addBulkReadingQuality(quality, filterDuplicates(bulkValidationRuleMap.get(quality.getTypeCode())));
        }
        return validationStatus;
    }

    abstract ChannelValidationContainer getChannelValidationContainer(Channel channel);

    abstract Multimap<String, IValidationRule> getMapQualityToRule(ChannelValidationContainer channelValidations);

    boolean wasValidated(Instant lastChecked, Instant readingTimestamp) {
        return lastChecked != null && readingTimestamp.compareTo(lastChecked) <= 0;
    }
}
