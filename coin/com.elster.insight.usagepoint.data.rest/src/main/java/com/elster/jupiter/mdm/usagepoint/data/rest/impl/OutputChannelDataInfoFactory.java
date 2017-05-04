/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.JournaledChannelReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.util.streams.ExtraCollectors;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class OutputChannelDataInfoFactory {

    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final ReadingQualityInfoFactory readingQualityInfoFactory;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;

    @Inject
    public OutputChannelDataInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory, ReadingQualityInfoFactory readingQualityInfoFactory, EstimationRuleInfoFactory estimationRuleInfoFactory) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.readingQualityInfoFactory = readingQualityInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
    }

    public OutputChannelDataInfo createChannelDataInfo(ChannelReadingWithValidationStatus readingWithValidationStatus) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.reportedDateTime = readingWithValidationStatus.getReportedDateTime();
        outputChannelDataInfo.interval = IntervalInfo.from(readingWithValidationStatus.getTimePeriod());
        outputChannelDataInfo.value = readingWithValidationStatus.getValue();
        if (readingWithValidationStatus.wasEdited()) {
            outputChannelDataInfo.calculatedValue = readingWithValidationStatus.getCalculatedValue();
        } else {
            outputChannelDataInfo.calculatedValue = null;
        }
        if (readingWithValidationStatus.getCalendar().isPresent()) {
            outputChannelDataInfo.calendarName = readingWithValidationStatus.getCalendar().get().getName();
        }
        outputChannelDataInfo.partOfTimeOfUseGap = readingWithValidationStatus.isPartOfTimeOfUseGap();
        setValidationFields(readingWithValidationStatus, outputChannelDataInfo);
        setEditingFields(readingWithValidationStatus, outputChannelDataInfo);
        setReadingQualities(readingWithValidationStatus, outputChannelDataInfo);
        return outputChannelDataInfo;
    }

    private void setValidationFields(ChannelReadingWithValidationStatus readingWithValidationStatus, OutputChannelDataInfo outputChannelDataInfo) {
        Optional<DataValidationStatus> validationStatus = readingWithValidationStatus.getValidationStatus();
        if (validationStatus.isPresent()) {
            DataValidationStatus status = validationStatus.get();
            outputChannelDataInfo.validationResult = ValidationStatus.forResult(status.getValidationResult());
            outputChannelDataInfo.dataValidated = status.completelyValidated();
            outputChannelDataInfo.action = decorate(status.getReadingQualities()
                    .stream())
                    .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                    .map(readingQuality -> readingQuality.getType().isSuspect() ? ValidationAction.FAIL : ValidationAction.WARN_ONLY)
                    .sorted(Comparator.reverseOrder())
                    .findFirst()
                    .orElse(null);
            outputChannelDataInfo.estimatedByRule = estimationRuleInfoFactory.createEstimationRuleInfo(status.getReadingQualities());
            if(outputChannelDataInfo.estimatedByRule != null) {
                outputChannelDataInfo.ruleId = outputChannelDataInfo.estimatedByRule.id;
            }
            outputChannelDataInfo.isProjected = status.getReadingQualities()
                    .stream()
                    .anyMatch(quality -> quality.getType().hasProjectedCategory());
            outputChannelDataInfo.isConfirmed = status.getReadingQualities()
                    .stream()
                    .anyMatch(quality -> quality.getType().isConfirmed());
            outputChannelDataInfo.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
        } else {
            // Missing value
            if (readingWithValidationStatus.isChannelValidationActive()
                    && readingWithValidationStatus.getChannelLastChecked().isPresent()
                    && !readingWithValidationStatus.getTimeStamp().isAfter(readingWithValidationStatus.getChannelLastChecked().get())) {
                outputChannelDataInfo.validationResult = ValidationStatus.OK;
                outputChannelDataInfo.dataValidated = true;
            } else {
                outputChannelDataInfo.validationResult = ValidationStatus.NOT_VALIDATED;
                outputChannelDataInfo.dataValidated = false;
            }
        }
    }

    private void setEditingFields(ChannelReadingWithValidationStatus readingWithValidationStatus, OutputChannelDataInfo outputChannelDataInfo) {
        readingWithValidationStatus.getReadingModificationFlag().ifPresent(modificationFlag -> {
            outputChannelDataInfo.modificationFlag = modificationFlag.getFirst();
            outputChannelDataInfo.editedInApp = modificationFlag.getLast().getType().system().map(ReadingModificationFlag::getApplicationInfo).orElse(null);
            if (modificationFlag.getLast() instanceof ReadingQualityRecord) {
                Instant timestamp = ((ReadingQualityRecord) modificationFlag.getLast()).getTimestamp();
                outputChannelDataInfo.modificationDate = timestamp;
                if (timestamp != null) {
                    outputChannelDataInfo.reportedDateTime = timestamp;
                }
            }
        });
    }

    private void setReadingQualities(ChannelReadingWithValidationStatus readingWithValidationStatus, OutputChannelDataInfo outputChannelDataInfo) {
        outputChannelDataInfo.readingQualities = readingWithValidationStatus.getReadingQualities().stream()
                .map(ReadingQuality::getType)
                .map(readingQualityInfoFactory::asInfo)
                .collect(Collectors.toList());
    }

    public OutputChannelDataInfo createEstimatedChannelDataInfo(IntervalReadingRecord readingRecord, BigDecimal estimatedValue) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.reportedDateTime = readingRecord.getTimeStamp();
        outputChannelDataInfo.interval = readingRecord.getTimePeriod().map(IntervalInfo::from).orElse(null);
        outputChannelDataInfo.value = estimatedValue;
        return outputChannelDataInfo;
    }

    public List<OutputChannelHistoryDataInfo> createOutputChannelHistoryDataInfo(Map<BaseReadingRecord, ChannelReadingWithValidationStatus> result) {
        List<OutputChannelHistoryDataInfo> infos = new ArrayList<>();
        result.forEach((record, readingWithValidationStatus) -> {
            if (record instanceof JournaledChannelReadingRecord) {
                OutputChannelHistoryDataInfo outputChannelDataInfo = new OutputChannelHistoryDataInfo(createChannelDataInfo(readingWithValidationStatus));
                outputChannelDataInfo.value = record.getValue();
                outputChannelDataInfo.journalTime = ((JournaledChannelReadingRecord) record).getJournalTime();
                outputChannelDataInfo.userName = ((JournaledChannelReadingRecord) record).getUserName();
                outputChannelDataInfo.reportedDateTime = record.getReportedDateTime();
                outputChannelDataInfo.readingQualities = record.getReadingQualities().stream()
                        .map(ReadingQuality::getType)
                        .map(readingQualityInfoFactory::asInfo)
                        .collect(Collectors.toList());
                infos.add(outputChannelDataInfo);
            }
            else {
                readingWithValidationStatus.setReadingRecord((AggregatedChannel.AggregatedIntervalReadingRecord)record);
                OutputChannelHistoryDataInfo outputChannelHistoryDataInfo = new OutputChannelHistoryDataInfo(createChannelDataInfo(readingWithValidationStatus));
                outputChannelHistoryDataInfo.userName = "";
                outputChannelHistoryDataInfo.readingQualities = record.getReadingQualities().stream()
                        .map(ReadingQuality::getType)
                        .map(readingQualityInfoFactory::asInfo)
                        .collect(Collectors.toList());
                infos.add(outputChannelHistoryDataInfo);
            }
        });
        return infos.stream().sorted(Comparator.comparing(info -> ((OutputChannelHistoryDataInfo)info).interval.end)
                .thenComparing(Comparator.comparing(info -> ((OutputChannelHistoryDataInfo) info).reportedDateTime).reversed())).collect(ExtraCollectors.toImmutableList());
    }
}
