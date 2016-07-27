package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.IntervalInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Optional;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class OutputChannelDataInfoFactory {

    private final ValidationRuleInfoFactory validationRuleInfoFactory;

    @Inject
    public OutputChannelDataInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
    }

    public OutputChannelDataInfo createChannelDataInfo(IntervalReadingWithValidationStatus readingWithValidationStatus) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.readingTime = readingWithValidationStatus.getTimeStamp();
        outputChannelDataInfo.interval = IntervalInfo.from(readingWithValidationStatus.getTimePeriod());
        outputChannelDataInfo.value = readingWithValidationStatus.getValue();

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
            outputChannelDataInfo.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
        } else {
            // Missing value
            outputChannelDataInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            outputChannelDataInfo.dataValidated = false;
        }
        return outputChannelDataInfo;
    }
}
