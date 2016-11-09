package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Optional;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class OutputRegisterDataInfoFactory {

    private final ValidationRuleInfoFactory validationRuleInfoFactory;

    @Inject
    public OutputRegisterDataInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
    }

    public OutputRegisterDataInfo createRegisterDataInfo(ReadingRecord readingRecord, Optional<DataValidationStatus> validationStatus) {
        OutputRegisterDataInfo info = new OutputRegisterDataInfo();
        info.timeStamp = readingRecord.getTimeStamp();
        info.value = readingRecord.getValue();
        info.reportedDateTime = readingRecord.getReportedDateTime();

        if (validationStatus.isPresent()) {
            DataValidationStatus status = validationStatus.get();
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
            info.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
        }

        return info;
    }
}
