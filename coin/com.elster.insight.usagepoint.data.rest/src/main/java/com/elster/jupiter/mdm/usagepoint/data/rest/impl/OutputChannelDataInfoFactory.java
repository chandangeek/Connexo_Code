/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class OutputChannelDataInfoFactory {

    private final ValidationRuleInfoFactory validationRuleInfoFactory;

    @Inject
    public OutputChannelDataInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
    }

    public OutputChannelDataInfo createChannelDataInfo(ReadingWithValidationStatus<IntervalReadingRecord> readingWithValidationStatus) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.reportedDateTime = readingWithValidationStatus.getTimeStamp();
        outputChannelDataInfo.interval = readingWithValidationStatus.getTimePeriod()
                .map(IntervalInfo::from)
                .orElse(null);
        outputChannelDataInfo.value = readingWithValidationStatus.getValue();
        outputChannelDataInfo.calculatedValue = readingWithValidationStatus.getCalculatedValue().orElse(null);
        readingWithValidationStatus.getReadingModificationFlag().ifPresent(modificationFlag -> {
            outputChannelDataInfo.modificationFlag = modificationFlag.getFirst();
            outputChannelDataInfo.editedInApp = modificationFlag.getLast().getType().system().map(ReadingModificationFlag::getApplicationInfo).orElse(null);
            outputChannelDataInfo.modificationDate = modificationFlag.getLast().getTimestamp();
        });

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
            outputChannelDataInfo.isConfirmed = status.getReadingQualities()
                    .stream()
                    .filter(quality -> quality.getType().isConfirmed())
                    .findFirst()
                    .isPresent();
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
        return outputChannelDataInfo;
    }

    public OutputChannelDataInfo createEstimatedChannelDataInfo(IntervalReadingRecord readingRecord, BigDecimal estimatedValue) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.reportedDateTime = readingRecord.getTimeStamp();
        outputChannelDataInfo.interval = readingRecord.getTimePeriod()
                .map(IntervalInfo::from)
                .orElse(null);
        outputChannelDataInfo.value = estimatedValue;
        return outputChannelDataInfo;
    }
}
