/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import javax.inject.Inject;
import java.util.Comparator;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class OutputRegisterDataInfoFactory {

    private final ValidationRuleInfoFactory validationRuleInfoFactory;

    @Inject
    public OutputRegisterDataInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
    }

    public OutputRegisterDataInfo createRegisterDataInfo(ReadingWithValidationStatus<ReadingRecord> readingWithValidationStatus, ReadingTypeDeliverable deliverable) {

        OutputRegisterDataInfo info = createRegisterDataInfo(readingWithValidationStatus, deliverable.getType());
        info.timeStamp = readingWithValidationStatus.getTimeStamp();
        info.reportedDateTime = readingWithValidationStatus.getReportedDateTime();
        readingWithValidationStatus.getReadingModificationFlag().ifPresent(modificationFlag -> {
            info.modificationFlag = modificationFlag.getFirst();
            info.editedInApp = modificationFlag.getLast()
                    .getType()
                    .system()
                    .map(ReadingModificationFlag::getApplicationInfo)
                    .orElse(null);
            info.modificationDate = modificationFlag.getLast().getTimestamp();
        });

        if ((deliverable.getType().equals(DeliverableType.NUMERICAL) || deliverable.getType()
                .equals(DeliverableType.BILLING))
                && readingWithValidationStatus.getValidationStatus().isPresent()) {
            DataValidationStatus status = readingWithValidationStatus.getValidationStatus().get();
            info.validationResult = ValidationStatus.forResult(status.getValidationResult());
            info.dataValidated = status.completelyValidated();
            info.action = decorate(status.getReadingQualities()
                    .stream())
                    .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                    .map(readingQuality -> readingQuality.getType()
                            .isSuspect() ? ValidationAction.FAIL : ValidationAction.WARN_ONLY)
                    .sorted(Comparator.reverseOrder())
                    .findFirst()
                    .orElse(null);
            ((NumericalOutputRegisterDataInfo) info).isConfirmed = status.getReadingQualities()
                    .stream()
                    .filter(quality -> quality.getType().isConfirmed())
                    .findFirst()
                    .isPresent();
            info.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
        }

        return info;
    }

    private OutputRegisterDataInfo createRegisterDataInfo(ReadingWithValidationStatus<ReadingRecord> readingWithValidationStatus, DeliverableType deliverableType) {
        switch (deliverableType) {
            case BILLING:
                BillingOutputRegisterDataInfo billingOutputRegisterDataInfo = new BillingOutputRegisterDataInfo();
                billingOutputRegisterDataInfo.value = readingWithValidationStatus.getValue();
                billingOutputRegisterDataInfo.calculatedValue = readingWithValidationStatus.getCalculatedValue()
                        .orElse(null);
                billingOutputRegisterDataInfo.interval = readingWithValidationStatus.getBillingPeriod()
                        .map(IntervalInfo::from)
                        .orElse(null);
                return billingOutputRegisterDataInfo;
            case TEXT:
                TextOutputRegisterDataInfo textOutputRegisterDataInfo = new TextOutputRegisterDataInfo();
                textOutputRegisterDataInfo.value = readingWithValidationStatus.getText().orElse(null);
                return textOutputRegisterDataInfo;
            case FLAGS:
                FlagsOutputRegisterDataInfo flagsOutputRegisterDataInfo = new FlagsOutputRegisterDataInfo();
                flagsOutputRegisterDataInfo.value = readingWithValidationStatus.getValue().longValue();
                return flagsOutputRegisterDataInfo;
            default:
                NumericalOutputRegisterDataInfo numericalOutputRegisterDataInfo = new NumericalOutputRegisterDataInfo();
                numericalOutputRegisterDataInfo.value = readingWithValidationStatus.getValue();
                numericalOutputRegisterDataInfo.calculatedValue = readingWithValidationStatus.getCalculatedValue()
                        .orElse(null);
                return numericalOutputRegisterDataInfo;
        }
    }
}
