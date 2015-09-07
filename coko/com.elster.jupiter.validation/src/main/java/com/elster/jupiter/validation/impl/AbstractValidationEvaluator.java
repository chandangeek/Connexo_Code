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

    public static final ReadingQualityType VALIDATED_AND_OK = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);

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

        for (BaseReading reading : readings) {
            boolean containsKey = readingQualities.containsKey(reading.getTimeStamp());
            List<ReadingQualityRecord> qualities = (containsKey ? new ArrayList<>(readingQualities.get(reading.getTimeStamp())) : new ArrayList<>());
            timesWithReadings.add(reading.getTimeStamp());
            boolean fullyValidated = configured && wasValidated(lastChecked, reading.getTimeStamp());
            if (fullyValidated) {
                addValidatedAndOkReadingQuality(reading.getTimeStamp(), channel, qualities);
            }
            result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities, Collections.emptyList(), validationRuleMap, null));
        }

        Set<Instant> timesWithoutReadings = new HashSet<>(readingQualities.keySet());
        timesWithoutReadings.removeAll(timesWithReadings);

        timesWithoutReadings.forEach(readingTimestamp -> {
            List<ReadingQualityRecord> qualities = new ArrayList<>(readingQualities.get(readingTimestamp));
            boolean fullyValidated = configured && wasValidated(lastChecked, readingTimestamp);
            if (fullyValidated) {
                addValidatedAndOkReadingQuality(readingTimestamp, channel, qualities);
            }
            result.add(createDataValidationStatusListFor(readingTimestamp, fullyValidated, qualities, Collections.emptyList(), validationRuleMap, null));
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

        for (BaseReading reading : readings) {
            boolean containsKey = readingQualities.containsKey(reading.getTimeStamp());
            List<ReadingQualityRecord> qualities = (containsKey ? new ArrayList<>(readingQualities.get(reading.getTimeStamp())) : new ArrayList<>());
            boolean bulkContainsKey = bulkReadingQualities.containsKey(reading.getTimeStamp());
            List<ReadingQualityRecord> bulkQualities = (bulkContainsKey ? new ArrayList<>(bulkReadingQualities.get(reading.getTimeStamp())) : new ArrayList<>());
            timesWithReadings.add(reading.getTimeStamp());
            boolean fullyValidated = configured && wasValidated(lastChecked, reading.getTimeStamp());
            if (fullyValidated) {
                addValidatedAndOkReadingQuality(reading.getTimeStamp(), mainChannel, qualities);
                addValidatedAndOkReadingQuality(reading.getTimeStamp(), bulkChannel, bulkQualities);
            }
            result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities, bulkQualities, validationRuleMap, bulkValidationRuleMap));
        }

        Set<Instant> timesWithoutReadings = new HashSet<>();
        timesWithoutReadings.addAll(readingQualities.keySet());
        timesWithoutReadings.addAll(bulkReadingQualities.keySet());
        timesWithoutReadings.removeAll(timesWithReadings);

        timesWithoutReadings.forEach(readingTimestamp -> {
            List<ReadingQualityRecord> qualities = readingQualities.containsKey(readingTimestamp) ? new ArrayList<>(readingQualities.get(readingTimestamp)) : new ArrayList();
            List<ReadingQualityRecord> bulkQualities = bulkReadingQualities.containsKey(readingTimestamp) ? new ArrayList<>(bulkReadingQualities.get(readingTimestamp)): new ArrayList<>();
            boolean fullyValidated = configured && wasValidated(lastChecked, readingTimestamp);
            if (fullyValidated) {
                addValidatedAndOkReadingQuality(readingTimestamp, mainChannel, qualities);
                addValidatedAndOkReadingQuality(readingTimestamp, bulkChannel, bulkQualities);
            }
            result.add(createDataValidationStatusListFor(readingTimestamp, fullyValidated, qualities, bulkQualities, validationRuleMap, bulkValidationRuleMap));
        });
        return result;
    }

    private void addValidatedAndOkReadingQuality(Instant readingTimeStamp, CimChannel channel, List<ReadingQualityRecord> qualities) {
        if (qualities.stream().noneMatch(ReadingQualityRecord::isSuspect)) {
            qualities.add(channel.createReadingQuality(VALIDATED_AND_OK, readingTimeStamp));
        }
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
