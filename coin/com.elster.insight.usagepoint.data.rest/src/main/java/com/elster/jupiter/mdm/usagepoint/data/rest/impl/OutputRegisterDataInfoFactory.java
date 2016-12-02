package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingRecord;
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

    public OutputRegisterDataInfo createRegisterDataInfo(ReadingWithValidationStatus<ReadingRecord> readingWithValidationStatus) {
        NumericalOutputRegisterDataInfo info = new NumericalOutputRegisterDataInfo();
        info.timeStamp = readingWithValidationStatus.getTimeStamp();
        info.value = readingWithValidationStatus.getValue();
        info.reportedDateTime = readingWithValidationStatus.getReportedDateTime();
        info.calculatedValue = readingWithValidationStatus.getCalculatedValue().orElse(null);
        readingWithValidationStatus.getReadingModificationFlag().ifPresent(modificationFlag -> {
            info.modificationFlag = modificationFlag.getFirst();
            info.editedInApp = modificationFlag.getLast()
                    .getType()
                    .system()
                    .map(ReadingModificationFlag::getApplicationInfo)
                    .orElse(null);
            info.modificationDate = modificationFlag.getLast().getTimestamp();
        });

        if (readingWithValidationStatus.getValidationStatus().isPresent()) {
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
            info.isConfirmed = status.getReadingQualities()
                    .stream()
                    .filter(quality -> quality.getType().isConfirmed())
                    .findFirst()
                    .isPresent();
            info.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
        }

        return info;
    }
}
