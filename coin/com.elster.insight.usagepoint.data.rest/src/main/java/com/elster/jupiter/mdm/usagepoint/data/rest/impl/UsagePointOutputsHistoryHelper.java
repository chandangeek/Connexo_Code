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
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsagePointOutputsHistoryHelper {

    private final ValidationService validationService;
    private final CalendarService calendarService;

    @Inject
    public UsagePointOutputsHistoryHelper(ValidationService validationService, CalendarService calendarService) {
        this.validationService = validationService;
        this.calendarService = calendarService;
    }

    public List<JournaledReadingRecord> collectHistoricalChannelData(Range<Instant> requestedInterval, UsagePoint usagePoint, MetrologyContract metrologyContract, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint, ReadingType readingType, boolean changedDataOnly) {
        List<JournaledReadingRecord> historicalChannelData = new ArrayList<>();
        effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)
                .ifPresent(channelsContainer -> {
                    Range<Instant> containerRange = channelsContainer.getInterval().toOpenClosedRange();
                    if (containerRange.isConnected(requestedInterval)) {
                        Range<Instant> effectiveInterval = containerRange.intersection(requestedInterval);
                        effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingType)
                                .ifPresent(aggregatedChannel -> historicalChannelData.addAll(collectHistoryFromAggregatedChannel(channelsContainer, usagePoint,
                                        readingType, aggregatedChannel, effectiveInterval, changedDataOnly)));
                    }
                });

        return historicalChannelData;
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

        Map<Instant, RegisterReadingWithValidationStatus> preFilledDataMap = journaledRegisterReadingRecords.stream()
                .map(ReadingRecord::getTimeStamp)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), readingWithValidationStatusFactory::createRegisterReading, (a, b) -> a, TreeMap::new));

        Map<Instant, List<JournaledReadingRecord>> historicalReadings = indexJournaledRecordsByTimestamp(journaledRegisterReadingRecords, preFilledDataMap.keySet());

        if (changedDataOnly) {
            historicalReadings = historicalReadings.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        List<? extends ReadingQualityRecord> readingQualityRecords = journaledRegisterReadingRecords.stream()
                .flatMap(record -> record.getReadingQualities().stream())
                .collect(Collectors.toList());

        setReadingQualitiesForRegisters(historicalReadings, usagePoint, effectiveInterval, aggregatedChannel, readingQualityRecords, readingType);
        setValidationStatusForRegisters(historicalReadings, preFilledDataMap, validationEvaluator, aggregatedChannel);

        return historicalReadings.values().stream()
                .flatMap(Collection::stream)
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

        setReadingQualitiesForChannels(aggregatedChannel.getReadingTypes(), readingQualitiesJournal, historicalReadings, readingQualityRecords);

        historicalReadings.forEach((instant, journaledReadingRecords) -> journaledReadingRecords.forEach(record -> {
            List<ReadingQualityRecord> readingQualityList = (List<ReadingQualityRecord>) record.getReadingQualities();
            DataValidationStatus dataValidationStatus = evaluator.getValidationStatus(
                    EnumSet.of(QualityCodeSystem.MDM),
                    aggregatedChannel,
                    record.getReportedDateTime(),
                    readingQualityList);
            record.setValidationStatus(dataValidationStatus);
            record.setInterval(preFilledChannelDataMap.get(instant).getTimePeriod());
        }));
    }

    private void setReadingQualitiesForChannels(List<? extends ReadingType> readingTypes, List<JournalEntry<? extends ReadingQualityRecord>> readingQualitiesJournal, Map<Instant,
            List<JournaledReadingRecord>> historicalReadings, List<? extends ReadingQualityRecord> readingQualities) {
        final List<JournalEntry<? extends ReadingQualityRecord>> finalReadingQualitiesJournal = readingQualitiesJournal;
        historicalReadings.entrySet().forEach(value -> {
            List<? extends ReadingQualityRecord> readingQualityList = readingQualities.stream()
                    .filter(readingQuality -> value.getKey().equals(readingQuality.getReadingTimestamp()))
                    .filter(readingQuality -> readingTypes.contains(readingQuality.getReadingType()))
                    .collect(Collectors.toList());
            List<? extends ReadingQualityRecord> journaledReadingQualities = finalReadingQualitiesJournal.stream()
                    .map(JournalEntry::get)
                    .filter(journaledReadingQuality -> value.getKey()
                            .equals(journaledReadingQuality.getReadingTimestamp()))
                    .filter(journaledReadingQuality -> readingTypes.contains(journaledReadingQuality.getReadingType()))
                    .collect(Collectors.toList());

            List<ReadingQualityRecord> mergedReadingQualities = new ArrayList<>();
            mergedReadingQualities.addAll(readingQualityList);
            mergedReadingQualities.addAll(journaledReadingQualities);

            List<? extends JournaledReadingRecord> records = value.getValue();

            mergedReadingQualities.forEach(mergedReadingQuality -> {
                Optional<? extends BaseReadingRecord> journaledReadingRecord;
                if (mergedReadingQuality.getTypeCode().equals("3.5.258") || mergedReadingQuality.getTypeCode()
                        .equals("3.5.259") || mergedReadingQuality.hasValidationCategory()) {
                    journaledReadingRecord = records.stream()
                            .filter(record -> record.getReportedDateTime().compareTo(mergedReadingQuality.getTimestamp()) <= 0)
                            .max(Comparator.comparing(JournaledReadingRecord::getReportedDateTime));
                } else {
                    journaledReadingRecord = records.stream()
                            .filter(record -> record.getReportedDateTime() != null)
                            .filter(record -> record.getReportedDateTime().compareTo(mergedReadingQuality.getTimestamp()) >= 0)
                            .min(Comparator.comparing(JournaledReadingRecord::getReportedDateTime));
                }
                journaledReadingRecord.ifPresent(record -> {
                    List<ReadingQualityRecord> qualityRecords = (List<ReadingQualityRecord>) ((JournaledReadingRecord) record).getReadingQualities();
                    qualityRecords.add(mergedReadingQuality);
                    ((JournaledReadingRecord) record).setReadingQualityRecords(qualityRecords.stream()
                            .distinct()
                            .collect(Collectors.toList()));
                });
            });
        });
    }

    private List<JournaledReadingRecord> putHistoricalDataFromChannel(UsagePoint usagePoint, ValidationEvaluator evaluator, AggregatedChannel aggregatedChannel, Range<Instant> effectiveInterval, List<? extends BaseReadingRecord> journaledChannelReadingRecords, Map<Instant, ChannelReadingWithValidationStatus> preFilledChannelDataMap, boolean changedDataOnly) {
        Map<Instant, List<JournaledReadingRecord>> historicalReadings = indexJournaledRecordsByTimestamp(journaledChannelReadingRecords, preFilledChannelDataMap.keySet());
        if (changedDataOnly) {
            historicalReadings = historicalReadings.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        setReadingQualitiesAndValidationStatuses(historicalReadings, usagePoint, evaluator, aggregatedChannel, journaledChannelReadingRecords, effectiveInterval, preFilledChannelDataMap);

        return historicalReadings.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Map<Instant, List<JournaledReadingRecord>> indexJournaledRecordsByTimestamp(List<? extends BaseReadingRecord> records, Set<Instant> intervalTimestamps) {
        return records.stream()
                .filter(record -> intervalTimestamps.contains(record.getTimeStamp()))
                .map(JournaledReadingRecord::new)
                .collect(Collectors.groupingBy(JournaledReadingRecord::getTimeStamp));
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

            List<ReadingQualityRecord> mergedReadingQualities = new ArrayList<>();
            mergedReadingQualities.addAll(readingQualityList);
            mergedReadingQualities.addAll(journaledQualities);

            List<JournaledReadingRecord> records = value.getValue();
            mergedReadingQualities.forEach(mergedReadingQuality -> {
                Optional<? extends JournaledReadingRecord> journalReadingOptional;
                journalReadingOptional = ((mergedReadingQuality.getTypeCode().equals("3.5.258")) || ((mergedReadingQuality.getTypeCode()
                        .equals("3.5.259")) || mergedReadingQuality.hasValidationCategory())) ?
                        records.stream()
                                .filter(record -> record.getReportedDateTime().compareTo(mergedReadingQuality.getTimestamp()) <= 0)
                                .max(Comparator.comparing(JournaledReadingRecord::getReportedDateTime)) :
                        records.stream()
                                .filter(record -> record.getReportedDateTime().compareTo(mergedReadingQuality.getTimestamp()) >= 0)
                                .min(Comparator.comparing(JournaledReadingRecord::getReportedDateTime));

                journalReadingOptional.ifPresent(reading -> {
                    List<ReadingQualityRecord> qualityRecords = (List<ReadingQualityRecord>) reading.getReadingQualities();
                    qualityRecords.add(mergedReadingQuality);
                    reading.setReadingQualityRecords(qualityRecords.stream()
                            .distinct()
                            .collect(Collectors.toList()));
                });
            });
        });
    }

    private void setValidationStatusForRegisters(Map<Instant, List<JournaledReadingRecord>> historicalReadings, Map<Instant, RegisterReadingWithValidationStatus> preFilledRegisterDataMap, ValidationEvaluator evaluator, AggregatedChannel aggregatedChannel) {
        historicalReadings.forEach((instant, journaledReadingRecords) -> journaledReadingRecords.forEach(record -> {
            List<ReadingQualityRecord> readingQualityList = (List<ReadingQualityRecord>) record.getReadingQualities();
            DataValidationStatus dataValidationStatus = evaluator.getValidationStatus(
                    EnumSet.of(QualityCodeSystem.MDM),
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
