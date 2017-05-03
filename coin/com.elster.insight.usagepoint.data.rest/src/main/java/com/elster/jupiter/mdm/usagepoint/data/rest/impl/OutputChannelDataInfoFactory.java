/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.aggregation.ReadingQualityCommentCategory;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;


public class OutputChannelDataInfoFactory {

    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final ReadingQualityInfoFactory readingQualityInfoFactory;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;
    private final MeteringService meteringService;

    @Inject
    public OutputChannelDataInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory, ReadingQualityInfoFactory readingQualityInfoFactory, EstimationRuleInfoFactory estimationRuleInfoFactory, MeteringService meteringService) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.readingQualityInfoFactory = readingQualityInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
        this.meteringService = meteringService;
    }

    public OutputChannelDataInfo createChannelDataInfo(ChannelReadingWithValidationStatus readingWithValidationStatus) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.reportedDateTime = readingWithValidationStatus.getTimeStamp();
        outputChannelDataInfo.interval = IntervalInfo.from(readingWithValidationStatus.getTimePeriod());
        outputChannelDataInfo.value = readingWithValidationStatus.getValue();
        outputChannelDataInfo.calculatedValue = readingWithValidationStatus.getCalculatedValue().orElse(null);
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
                    .map(ReadingQuality::getType)
                    .anyMatch(ReadingQualityType::hasProjectedCategory);

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
            if(modificationFlag.getLast() instanceof ReadingQualityRecord){
                outputChannelDataInfo.modificationDate = ((ReadingQualityRecord)modificationFlag.getLast()).getTimestamp();
            }
        });
    }

    private void setReadingQualities(ChannelReadingWithValidationStatus readingWithValidationStatus, OutputChannelDataInfo outputChannelDataInfo) {
        List<ReadingQualityInfo> readingQualityInfos = readingWithValidationStatus.getReadingQualities().stream()
                .map(readingQualityInfoFactory::asInfo)
                .collect(Collectors.toList());
        outputChannelDataInfo.readingQualities = readingQualityInfos;
        readingQualityInfos.stream()
                .filter(readingQualityInfo -> readingQualityInfo.comment != null)
                .findFirst()
                .ifPresent(readingQuality -> {
                    outputChannelDataInfo.commentId = getEstimationCommentIdByValue(readingQuality.comment);
                    outputChannelDataInfo.commentValue = readingQuality.comment;
                });
    }

    public OutputChannelDataInfo createUpdatedChannelDataInfo(IntervalReadingRecord readingRecord, BigDecimal newValue, boolean isProjected, Optional<ReadingQualityComment> readingQualityComment) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.reportedDateTime = readingRecord.getReportedDateTime();
        readingRecord.getReadingType().getIntervalLength().ifPresent(intervalLength -> {
            Instant readingTimeStamp = readingRecord.getTimeStamp();
            outputChannelDataInfo.interval = IntervalInfo.from(Range.openClosed(readingTimeStamp.minus(intervalLength), readingTimeStamp));
        });
        outputChannelDataInfo.value = newValue;
        outputChannelDataInfo.isProjected = isProjected;
        readingQualityComment.ifPresent(comment -> {
            outputChannelDataInfo.commentId = comment.getId();
            outputChannelDataInfo.commentValue = comment.getComment();
        });
        return outputChannelDataInfo;
    }

    private long getEstimationCommentIdByValue(String commentValue) {
         return meteringService.getAllReadingQualityComments(ReadingQualityCommentCategory.ESTIMATION)
                .stream()
                .filter(readingQualityComment -> readingQualityComment.getComment().equals(commentValue))
                .map(ReadingQualityComment::getId)
                .findFirst().orElse(0L);
    }

    public PrevalidatedChannelDataInfo createPrevalidatedChannelDataInfo(DataValidationStatus dataValidationStatus) {
        PrevalidatedChannelDataInfo info = new PrevalidatedChannelDataInfo();
        info.readingTime = dataValidationStatus.getReadingTimestamp();
        info.validationResult = ValidationStatus.forResult(dataValidationStatus.getValidationResult());
        info.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(dataValidationStatus);
        return info;
    }
}
