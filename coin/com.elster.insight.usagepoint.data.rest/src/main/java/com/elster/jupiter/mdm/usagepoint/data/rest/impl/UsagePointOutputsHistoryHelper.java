/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsagePointOutputsHistoryHelper {

    private static final String INTERVAL_START = "intervalStart";
    private static final String INTERVAL_END = "intervalEnd";

    private final ValidationService validationService;
    private final CalendarService calendarService;

    @Inject
    public UsagePointOutputsHistoryHelper(ValidationService validationService, CalendarService calendarService) {
        this.validationService = validationService;
        this.calendarService = calendarService;
    }

    public List<JournaledReadingRecord> collectHistoricalChannelData(JsonQueryFilter filter, UsagePoint usagePoint, MetrologyContract metrologyContract, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint, ReadingType readingType, boolean changedDataOnly) {
        if (filter.hasProperty(INTERVAL_START) && filter.hasProperty(INTERVAL_END)) {
            List<JournaledReadingRecord> result = new ArrayList<>();
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant(INTERVAL_START), filter.getInstant(INTERVAL_END));
            effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)
                    .ifPresent(channelsContainer -> {
                        Range<Instant> containerRange = channelsContainer.getInterval().toOpenClosedRange();
                        if (containerRange.isConnected(requestedInterval)) {
                            Range<Instant> effectiveInterval = containerRange.intersection(requestedInterval);
                            effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingType)
                                    .ifPresent(aggregatedChannel -> result.addAll(collectHistoryFromAggregatedChannel(channelsContainer, usagePoint,
                                            readingType, aggregatedChannel, effectiveInterval, changedDataOnly)));
                        }
                    });
            return result;
        }
        return Collections.emptyList();
    }

    public Set<JournaledReadingRecord> collectHistoricalRegisterData(UsagePoint usagePoint, AggregatedChannel aggregatedChannel, Range<Instant> effectiveInterval, ReadingType readingType, boolean changedDataOnly) {
        List<? extends ReadingRecord> journaledRegisterReadingRecords = aggregatedChannel.getJournaledRegisterReadings(readingType, effectiveInterval);
        ValidationEvaluator validationEvaluator = validationService.getEvaluator();

        ReadingWithValidationStatusFactory readingWithValidationStatusFactory = new ReadingWithValidationStatusFactory(
                aggregatedChannel,
                validationEvaluator.isValidationEnabled(aggregatedChannel),
                validationEvaluator.getLastChecked(aggregatedChannel.getChannelsContainer(), aggregatedChannel.getMainReadingType())
                        .orElse(null),
                usagePoint,
                calendarService);
        Map<Instant, List<JournaledReadingRecord>> historicalReadings = mapJournaledRecordsByTimestamp(journaledRegisterReadingRecords);

        if (changedDataOnly) {
            historicalReadings = historicalReadings.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        Map<Instant, RegisterReadingWithValidationStatus> preFilledDataMap = journaledRegisterReadingRecords.stream()
                .map(ReadingRecord::getTimeStamp)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), readingWithValidationStatusFactory::createRegisterReading, (a, b) -> a, TreeMap::new));
        List<? extends ReadingQualityRecord> readingQualityRecords = journaledRegisterReadingRecords.stream()
                .flatMap(record -> record.getReadingQualities().stream())
                .collect(Collectors.toList());

        setReadingQualitiesForRegisters(historicalReadings, usagePoint, effectiveInterval, aggregatedChannel, readingQualityRecords, readingType);
        setValidationStatusForRegisters(historicalReadings, preFilledDataMap, validationEvaluator, aggregatedChannel);

        return historicalReadings.entrySet()
                .stream()
                .flatMap(value -> value.getValue().stream())
                .collect(Collectors.toSet());
    }

    private List<JournaledReadingRecord> collectHistoryFromAggregatedChannel(ChannelsContainer channelsContainer, UsagePoint usagePoint, ReadingType readingType,
                                                                             AggregatedChannel aggregatedChannel, Range<Instant> effectiveInterval, boolean changedDataOnly) {
        List<? extends BaseReadingRecord> journaledChannelReadingRecords = aggregatedChannel.getJournaledChannelReadings(readingType, effectiveInterval);
        ValidationEvaluator evaluator = validationService.getEvaluator();
        ReadingWithValidationStatusFactory readingWithValidationStatusFactory = new ReadingWithValidationStatusFactory(
                aggregatedChannel,
                evaluator.isValidationEnabled(aggregatedChannel),
                evaluator.getLastChecked(channelsContainer, aggregatedChannel.getMainReadingType()).orElse(null),
                usagePoint,
                this.calendarService);
        Map<Instant, ChannelReadingWithValidationStatus> preFilledChannelDataMap = aggregatedChannel.toList(effectiveInterval)
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        readingWithValidationStatusFactory::createChannelReading, (r1, r2) -> r1, TreeMap::new));

        return putHistoricalDataFromChannel(usagePoint, evaluator, aggregatedChannel, effectiveInterval, journaledChannelReadingRecords, preFilledChannelDataMap, changedDataOnly);
    }

    private void setReadingQualitiesAndValidationStatuses(Map<Instant, List<JournaledReadingRecord>> historicalReadings, UsagePoint usagePoint, ValidationEvaluator evaluator, AggregatedChannel aggregatedChannel, List<? extends BaseReadingRecord> journaledChannelReadingRecords,
                                                          Range<Instant> effectiveInterval, Map<Instant, ChannelReadingWithValidationStatus> preFilledChannelDataMap) {
        List<JournalEntry<? extends ReadingQualityRecord>> readingQualitiesJournal = usagePoint
                .getJournaledReadingQualities(effectiveInterval, aggregatedChannel);
        List<? extends ReadingQualityRecord> readingQualityRecords = journaledChannelReadingRecords.stream()
                .flatMap(record -> record.getReadingQualities().stream())
                .collect(Collectors.toList());

        setReadingQualities(aggregatedChannel.getReadingTypes(), readingQualitiesJournal, historicalReadings, readingQualityRecords);

        historicalReadings.forEach((instant, journaledReadingRecords) -> journaledReadingRecords.forEach(record -> {
            List<ReadingQualityRecord> readingQualityList = (List<ReadingQualityRecord>) record.getReadingQualities();
            DataValidationStatus dataValidationStatus = evaluator.getValidationStatus(
                    EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC),
                    aggregatedChannel,
                    instant,
                    readingQualityList);
            record.setValidationStatus(dataValidationStatus);
            record.setInterval(preFilledChannelDataMap.get(instant).getTimePeriod());
        }));
    }

    private void setReadingQualities(List<? extends ReadingType> readingTypes, List<JournalEntry<? extends ReadingQualityRecord>> readingQualitiesJournal, Map<Instant,
            List<JournaledReadingRecord>> historicalReadings, List<? extends ReadingQualityRecord> readingQualities) {
        final List<JournalEntry<? extends ReadingQualityRecord>> finalReadingQualitiesJournal = readingQualitiesJournal;
        historicalReadings.entrySet().forEach(value -> {
            List<? extends ReadingQualityRecord> readingQualityList = readingQualities.stream()
                    .filter(readingQuality -> value.getKey().equals(readingQuality.getReadingTimestamp()))
                    .filter(readingQuality -> readingTypes.contains(readingQuality.getReadingType()))
                    .collect(Collectors.toList());
            List<? extends ReadingQualityRecord> journaledReadingQualities = finalReadingQualitiesJournal.stream()
                    .filter(journaledReadingQuality -> value.getKey()
                            .equals(journaledReadingQuality.get().getReadingTimestamp()))
                    .filter(journaledReadingQuality -> readingTypes.contains(journaledReadingQuality.get()
                            .getReadingType()))
                    .map(JournalEntry::get)
                    .collect(Collectors.toList());
            List<? extends ReadingQualityRecord> mergedReadingQualities = new ArrayList<>();
            mergedReadingQualities.addAll(new ArrayList(readingQualityList));
            mergedReadingQualities.addAll(new ArrayList(journaledReadingQualities));
            mergedReadingQualities.forEach(mergedReadingQuality -> {
                Optional<? extends BaseReadingRecord> journaledReadingRecord;
                if (mergedReadingQuality.getTypeCode().compareTo("3.5.258") == 0 || mergedReadingQuality.getTypeCode()
                        .compareTo("3.5.259") == 0) {
                    journaledReadingRecord = value.getValue().stream()
                            .sorted(Comparator.comparing(BaseReading::getTimeStamp).reversed())
                            .filter(record -> record.getTimeStamp().equals(mergedReadingQuality.getReadingTimestamp()))
                            .findFirst();
                } else {
                    journaledReadingRecord = value.getValue().stream()
                            .sorted(Comparator.comparing(BaseReading::getTimeStamp))
                            .filter(record -> record.getTimeStamp().equals(mergedReadingQuality.getReadingTimestamp()))
                            .findFirst();
                }
                journaledReadingRecord.ifPresent(journalReadingRecord -> {
                    List<ReadingQualityRecord> qualityRecords = (List<ReadingQualityRecord>) ((JournaledReadingRecord) journalReadingRecord)
                            .getReadingRecordQualities();
                    qualityRecords.add(mergedReadingQuality);
                    ((JournaledReadingRecord) journaledReadingRecord.get()).setReadingQualityRecords(qualityRecords);
                });
            });
        });
    }

    private List<JournaledReadingRecord> putHistoricalDataFromChannel(UsagePoint usagePoint, ValidationEvaluator evaluator, AggregatedChannel aggregatedChannel, Range<Instant> effectiveInterval, List<? extends BaseReadingRecord> journaledChannelReadingRecords, Map<Instant, ChannelReadingWithValidationStatus> preFilledChannelDataMap, boolean changedDataOnly) {
        Map<Instant, List<JournaledReadingRecord>> historicalReadings = mapJournaledRecordsByTimestamp(journaledChannelReadingRecords);
        if (changedDataOnly) {
            historicalReadings = historicalReadings.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        setReadingQualitiesAndValidationStatuses(historicalReadings, usagePoint, evaluator, aggregatedChannel, journaledChannelReadingRecords, effectiveInterval, preFilledChannelDataMap);

        Map<BaseReadingRecord, ChannelReadingWithValidationStatus> map = new HashMap<>();
        historicalReadings.entrySet().stream().map(Map.Entry::getValue)
                .forEach(baseReadingRecords ->
                        baseReadingRecords.forEach(baseReadingRecord ->
                                map.put(baseReadingRecord, preFilledChannelDataMap.get(baseReadingRecord.getTimeStamp()))));
        List<JournaledReadingRecord> result = historicalReadings.entrySet()
                .stream()
                .flatMap(value -> value.getValue().stream())
                .collect(Collectors.toList());

        return result;
    }

    private Map<Instant, List<JournaledReadingRecord>> mapJournaledRecordsByTimestamp(List<? extends BaseReadingRecord> records) {
        Map<Instant, List<JournaledReadingRecord>> result = new HashMap<>();
        records.stream().map(JournaledReadingRecord::new).forEach(record -> {
            List<JournaledReadingRecord> readingRecords = result.get(record.getTimeStamp());
            if (readingRecords == null) {
                readingRecords = new ArrayList<>();
                readingRecords.add(record);
                result.put(record.getTimeStamp(), readingRecords);
            } else {
                readingRecords.add(record);
                result.put(record.getTimeStamp(), readingRecords);
            }
        });

        return result;
    }

    private void setReadingQualitiesForRegisters(Map<Instant, List<JournaledReadingRecord>> historicalReadings, UsagePoint usagePoint, Range<Instant> effectiveInterval, AggregatedChannel aggregatedChannel, List<? extends ReadingQualityRecord> readingQualities, ReadingType readingType) {
        List<JournalEntry<? extends ReadingQualityRecord>> journaledReadingQualities = usagePoint.getJournaledReadingQualities(effectiveInterval, aggregatedChannel)
                .stream()
                .filter(journalEntry -> journalEntry.get().getReadingType().equals(readingType))
                .collect(Collectors.toList());
        historicalReadings.entrySet().forEach(value -> {
            List<? extends ReadingQualityRecord> readingQualityList = readingQualities.stream()
                    .filter(readingQuality -> value.getKey().equals(readingQuality.getReadingTimestamp()))
                    .filter(readingQuality -> readingType.equals(readingQuality.getReadingType()))
                    .collect(Collectors.toList());
            List<? extends ReadingQualityRecord> journaledQualities = journaledReadingQualities.stream()
                    .filter(journaledReadingQuality -> value.getKey()
                            .equals(journaledReadingQuality.get().getReadingTimestamp()))
                    .filter(journaledReadingQuality -> readingType.equals(journaledReadingQuality.get()
                            .getReadingType()))
                    .map(JournalEntry::get)
                    .collect(Collectors.toList());
            List<? extends ReadingQualityRecord> mergedReadingQualities = new ArrayList<>();
            mergedReadingQualities.addAll(new ArrayList(readingQualityList));
            mergedReadingQualities.addAll(new ArrayList(journaledQualities));

            mergedReadingQualities.forEach(mergedReadingQuality -> {
                Optional<? extends JournaledReadingRecord> journalReadingOptional;
                journalReadingOptional = ((mergedReadingQuality.getTypeCode()
                        .compareTo("3.5.258") == 0) || ((mergedReadingQuality.getTypeCode()
                        .compareTo("3.5.259") == 0))) ?
                        value.getValue().stream().sorted(Comparator.comparing(BaseReading::getTimeStamp).reversed())
                                .filter(record -> record.getTimeStamp()
                                        .equals(mergedReadingQuality.getReadingTimestamp())).findFirst() :
                        value.getValue().stream().sorted(Comparator.comparing(BaseReading::getTimeStamp))
                                .filter(record -> record.getTimeStamp()
                                        .equals(mergedReadingQuality.getReadingTimestamp())).findFirst();

                journalReadingOptional.ifPresent(reading -> {
                    List<ReadingQualityRecord> qualityRecords = (List<ReadingQualityRecord>) reading.getReadingRecordQualities();
                    qualityRecords.add(mergedReadingQuality);
                    reading.setReadingQualityRecords(qualityRecords);
                });
            });
        });
    }

    private void setValidationStatusForRegisters(Map<Instant, List<JournaledReadingRecord>> historicalReadings, Map<Instant, RegisterReadingWithValidationStatus> preFilledRegisterDataMap, ValidationEvaluator evaluator, AggregatedChannel aggregatedChannel) {
        historicalReadings.forEach((instant, journaledReadingRecords) -> journaledReadingRecords.forEach(record -> {
            List<ReadingQualityRecord> readingQualityList = (List<ReadingQualityRecord>) record.getReadingQualities();
            DataValidationStatus dataValidationStatus = evaluator.getValidationStatus(
                    EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC),
                    aggregatedChannel,
                    instant,
                    readingQualityList);
            record.setValidationStatus(dataValidationStatus);
            RegisterReadingWithValidationStatus registerReadingWithValidationStatus = preFilledRegisterDataMap.get(instant);
            registerReadingWithValidationStatus.setValidationStatus(dataValidationStatus);
            registerReadingWithValidationStatus.setPersistedReadingRecord((ReadingRecord) record.getStoredReadingRecord());
            record.setInterval(registerReadingWithValidationStatus.getTimePeriod().get());
        }));
    }
}
