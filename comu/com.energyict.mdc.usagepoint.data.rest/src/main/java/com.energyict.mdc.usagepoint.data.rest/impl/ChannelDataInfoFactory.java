package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.energyict.mdc.common.rest.IntervalInfo;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class ChannelDataInfoFactory {

    private final Thesaurus thesaurus;
    private final ValidationRuleInfoFactory validationRuleInfoFactory;

    @Inject
    public ChannelDataInfoFactory(Thesaurus thesaurus,
                                  ValidationRuleInfoFactory validationRuleInfoFactory) {
        this.thesaurus = thesaurus;
        this.validationRuleInfoFactory = validationRuleInfoFactory;
    }

    public ChannelDataInfo createChannelDataInfo(IntervalReadingRecord readingRecord,
                                                 List<DataValidationStatus> validationStatuses,
                                                 boolean withRules) {

        ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();
        channelIntervalInfo.interval = IntervalInfo.from(readingRecord.getTimePeriod().get());
        channelIntervalInfo.readingTime = readingRecord.getTimeStamp();
        channelIntervalInfo.value = readingRecord.getValue();
        Optional<DataValidationStatus> dataValidationStatus = validationStatuses
                .stream()
                .filter(validationStatus -> validationStatus.getReadingTimestamp().equals(readingRecord.getTimeStamp()))
                .findFirst();
        channelIntervalInfo.validationResult = ValidationStatus.forResult(dataValidationStatus.get().getValidationResult());
        channelIntervalInfo.dataValidated = dataValidationStatus.get().completelyValidated();
        channelIntervalInfo.validationAction = decorate(readingRecord.getReadingQualities()
                .stream())
                .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                .map(readingQuality -> readingQuality.getType().isSuspect() ? ValidationAction.FAIL : ValidationAction.WARN_ONLY)
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .orElse(null);
        if (withRules) {
            channelIntervalInfo.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(dataValidationStatus.get());
        }
        return channelIntervalInfo;
    }
}