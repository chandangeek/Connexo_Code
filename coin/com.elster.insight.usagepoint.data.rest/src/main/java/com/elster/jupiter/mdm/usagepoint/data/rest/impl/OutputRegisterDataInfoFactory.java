/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class OutputRegisterDataInfoFactory {

    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final ReadingQualityInfoFactory readingQualityInfoFactory;

    @Inject
    public OutputRegisterDataInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory, ReadingQualityInfoFactory readingQualityInfoFactory) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.readingQualityInfoFactory = readingQualityInfoFactory;
    }

    public OutputRegisterDataInfo createRegisterDataInfo(RegisterReadingWithValidationStatus readingWithValidationStatus, ReadingTypeDeliverable deliverable) {
        OutputRegisterDataInfo info = createRegisterDataInfo(readingWithValidationStatus, deliverable.getType());
        info.timeStamp = readingWithValidationStatus.getTimeStamp();
        info.reportedDateTime = readingWithValidationStatus.getReportedDateTime();

        if (isNumerical(deliverable)) {
            setValidationFields(readingWithValidationStatus, info);
        }
        setEditingFields(readingWithValidationStatus, info);
        setReadingQualities(readingWithValidationStatus, info);
        return info;
    }

    private OutputRegisterDataInfo createRegisterDataInfo(RegisterReadingWithValidationStatus readingWithValidationStatus, DeliverableType deliverableType) {
        switch (deliverableType) {
            case BILLING:
                BillingOutputRegisterDataInfo billingOutputRegisterDataInfo = new BillingOutputRegisterDataInfo();
                billingOutputRegisterDataInfo.value = readingWithValidationStatus.getValue();
                billingOutputRegisterDataInfo.calculatedValue = readingWithValidationStatus.getCalculatedValue().orElse(null);
                billingOutputRegisterDataInfo.interval = readingWithValidationStatus.getBillingPeriod().map(IntervalInfo::from).orElse(null);
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
                numericalOutputRegisterDataInfo.calculatedValue = readingWithValidationStatus.getCalculatedValue().orElse(null);
                return numericalOutputRegisterDataInfo;
        }
    }

    private boolean isNumerical(ReadingTypeDeliverable deliverable) {
        return deliverable.getType().equals(DeliverableType.NUMERICAL) || deliverable.getType().equals(DeliverableType.BILLING);
    }

    private void setValidationFields(RegisterReadingWithValidationStatus readingWithValidationStatus, OutputRegisterDataInfo info) {
        if (readingWithValidationStatus.getValidationStatus().isPresent()) {
            DataValidationStatus status = readingWithValidationStatus.getValidationStatus().get();
            info.validationResult = ValidationStatus.forResult(status.getValidationResult());
            info.dataValidated = status.completelyValidated();
            info.action = decorate(status.getReadingQualities().stream())
                    .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                    .map(readingQuality -> readingQuality.getType().isSuspect() ? ValidationAction.FAIL : ValidationAction.WARN_ONLY)
                    .sorted(Comparator.reverseOrder())
                    .findFirst()
                    .orElse(null);
            ((NumericalOutputRegisterDataInfo) info).isConfirmed = status.getReadingQualities().stream()
                    .filter(quality -> quality.getType().isConfirmed())
                    .findFirst()
                    .isPresent();
            info.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
        }
    }

    private void setEditingFields(RegisterReadingWithValidationStatus readingWithValidationStatus, OutputRegisterDataInfo info) {
        readingWithValidationStatus.getReadingModificationFlag().ifPresent(modificationFlag -> {
            info.modificationFlag = modificationFlag.getFirst();
            info.editedInApp = modificationFlag.getLast()
                    .getType()
                    .system()
                    .map(ReadingModificationFlag::getApplicationInfo)
                    .orElse(null);
            if(modificationFlag.getLast() instanceof ReadingQualityRecord){
                info.modificationDate = ((ReadingQualityRecord)modificationFlag.getLast()).getTimestamp();
            }
        });
    }

    private void setReadingQualities(RegisterReadingWithValidationStatus readingWithValidationStatus, OutputRegisterDataInfo info) {
        info.readingQualities = readingWithValidationStatus.getReadingQualities().stream()
                .map(ReadingQuality::getType)
                .map(readingQualityInfoFactory::asInfo)
                .collect(Collectors.toList());
    }
}
