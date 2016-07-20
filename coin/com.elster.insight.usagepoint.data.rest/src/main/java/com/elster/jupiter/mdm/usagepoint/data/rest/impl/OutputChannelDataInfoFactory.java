package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.IntervalInfo;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class OutputChannelDataInfoFactory {

    private final ValidationRuleInfoFactory validationRuleInfoFactory;

    @Inject
    public OutputChannelDataInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
    }

    public OutputChannelDataInfo createChannelDataInfo(IntervalReadingRecord intervalReadingRecord, List<DataValidationStatus> dataValidationStatuses) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.value = intervalReadingRecord.getValue();
        outputChannelDataInfo.interval = IntervalInfo.from(intervalReadingRecord.getTimePeriod().get());
        outputChannelDataInfo.readingTime = intervalReadingRecord.getTimeStamp();
        DataValidationStatus validationStatus = dataValidationStatuses
                .stream()
                .filter(dataValidationStatus -> dataValidationStatus.getReadingTimestamp().equals(intervalReadingRecord.getTimeStamp()))
                .findFirst()
                .get();
        outputChannelDataInfo.validationResult = ValidationStatus.forResult(validationStatus.getValidationResult());
        outputChannelDataInfo.dataValidated = validationStatus.completelyValidated();
        outputChannelDataInfo.action = decorate(validationStatus.getReadingQualities()
                .stream())
                .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                .map(readingQuality -> readingQuality.getType().isSuspect() ? ValidationAction.FAIL : ValidationAction.WARN_ONLY)
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .orElse(null);
        outputChannelDataInfo.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(validationStatus);
        return outputChannelDataInfo;
    }
}
