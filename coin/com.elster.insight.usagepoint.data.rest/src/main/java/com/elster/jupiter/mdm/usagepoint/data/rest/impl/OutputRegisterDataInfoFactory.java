/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.JournaledRegisterReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class OutputRegisterDataInfoFactory {
    private final List<Aggregate> aggregatesWithEventDate = Arrays.asList(Aggregate.MAXIMUM, Aggregate.FIFTHMAXIMIMUM,
            Aggregate.FOURTHMAXIMUM, Aggregate.MINIMUM, Aggregate.SECONDMAXIMUM, Aggregate.SECONDMINIMUM, Aggregate.THIRDMAXIMUM);

    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final ReadingQualityInfoFactory readingQualityInfoFactory;

    @Inject
    public OutputRegisterDataInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory, ReadingQualityInfoFactory readingQualityInfoFactory) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.readingQualityInfoFactory = readingQualityInfoFactory;
    }

    public OutputRegisterDataInfo createRegisterDataInfo(RegisterReadingWithValidationStatus readingWithValidationStatus, ReadingTypeDeliverable deliverable) {
        OutputRegisterDataInfo info = createRegisterDataInfo(readingWithValidationStatus, deliverable.getType(), deliverable.getReadingType());
        info.timeStamp = readingWithValidationStatus.getTimeStamp();
        info.reportedDateTime = readingWithValidationStatus.getReportedDateTime();

        if (isNumerical(deliverable)) {
            setValidationFields(readingWithValidationStatus, info);
        }
        setEditingFields(readingWithValidationStatus, info);
        setReadingQualities(readingWithValidationStatus, info);
        return info;
    }

    private OutputRegisterDataInfo createRegisterDataInfo(RegisterReadingWithValidationStatus readingWithValidationStatus, DeliverableType deliverableType, ReadingType readingType) {
        switch (deliverableType) {
            case BILLING:
                BillingOutputRegisterDataInfo billingOutputRegisterDataInfo = new BillingOutputRegisterDataInfo();
                billingOutputRegisterDataInfo.value = readingWithValidationStatus.getValue();
                billingOutputRegisterDataInfo.calculatedValue = readingWithValidationStatus.getCalculatedValue().orElse(null);
                billingOutputRegisterDataInfo.interval = readingWithValidationStatus.getBillingPeriod().map(IntervalInfo::from).orElse(null);
                if (aggregatesWithEventDate.contains(readingType.getAggregate())) {
                    billingOutputRegisterDataInfo.eventDate = readingWithValidationStatus.getEventDate().orElse(null);
                }
                if (readingType.isCumulative()) {
                    billingOutputRegisterDataInfo.deltaValue = readingWithValidationStatus.getDeltaValue();
                }
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

                numericalOutputRegisterDataInfo.interval = readingWithValidationStatus.getTimePeriod()
                        .map(IntervalInfo::from)
                        .orElse(null);
                if (aggregatesWithEventDate.contains(readingType.getAggregate())) {
                    numericalOutputRegisterDataInfo.eventDate = readingWithValidationStatus.getEventDate().orElse(null);
                }
                if (readingType.isCumulative()) {
                    numericalOutputRegisterDataInfo.deltaValue = readingWithValidationStatus.getDeltaValue();
                }
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
                    .anyMatch(quality -> quality.getType().isConfirmed());
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
            if (modificationFlag.getLast() instanceof ReadingQualityRecord) {
                info.modificationDate = ((ReadingQualityRecord) modificationFlag.getLast()).getTimestamp();
            }
        });
    }

    private void setReadingQualities(RegisterReadingWithValidationStatus readingWithValidationStatus, OutputRegisterDataInfo info) {
        info.readingQualities = readingWithValidationStatus.getReadingQualities().stream()
                .map(readingQualityInfoFactory::asInfo)
                .collect(Collectors.toList());
    }

    public List<OutputRegisterHistoryDataInfo> createHistoricalRegisterInfo(Set<JournaledReadingRecord> readingRecords) {
        List<OutputRegisterHistoryDataInfo> data = new ArrayList<>();
        readingRecords.forEach(journaledReadingRecord -> {
            OutputRegisterHistoryDataInfo outputRegisterHistoryDataInfo = new OutputRegisterHistoryDataInfo();
            BaseReadingRecord record = journaledReadingRecord.getStoredReadingRecord();
            DataValidationStatus status = journaledReadingRecord.getValidationStatus();
            outputRegisterHistoryDataInfo.interval = IntervalInfo.from(journaledReadingRecord.getInterval());
            outputRegisterHistoryDataInfo.userName = record instanceof JournaledRegisterReadingRecord ?
                    ((JournaledRegisterReadingRecord) record).getUserName() : "";
            outputRegisterHistoryDataInfo.timeStamp = record.getReportedDateTime();
            outputRegisterHistoryDataInfo.value = record.getValue();
            outputRegisterHistoryDataInfo.reportedDateTime = record.getReportedDateTime();
            outputRegisterHistoryDataInfo.readingQualities = journaledReadingRecord.getReadingQualities().stream()
                    .map(readingQualityInfoFactory::asInfo)
                    .collect(Collectors.toList());
            outputRegisterHistoryDataInfo.dataValidated = status.completelyValidated();
            outputRegisterHistoryDataInfo.validationResult = ValidationStatus.forResult(status.getValidationResult());
            outputRegisterHistoryDataInfo.validationAction = decorate(status.getReadingQualities()
                    .stream())
                    .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                    .map(readingQuality -> readingQuality.getType().isSuspect() ? ValidationAction.FAIL : ValidationAction.WARN_ONLY)
                    .sorted(Comparator.reverseOrder())
                    .findFirst()
                    .orElse(null);

            outputRegisterHistoryDataInfo.isConfirmed = status.getReadingQualities().stream()
                    .anyMatch(quality -> quality.getType().isConfirmed());

            extractModificationFlag(journaledReadingRecord, journaledReadingRecord.getValidationStatus()).ifPresent(modificationFlag -> {
                outputRegisterHistoryDataInfo.modificationFlag = modificationFlag.getFirst();
                outputRegisterHistoryDataInfo.editedInApp = modificationFlag.getLast().getType().system().map(ReadingModificationFlag::getApplicationInfo).orElse(null);
                if (modificationFlag.getLast() instanceof ReadingQualityRecord) {
                    Instant timestamp = ((ReadingQualityRecord) modificationFlag.getLast()).getTimestamp();
                    outputRegisterHistoryDataInfo.modificationDate = timestamp;
                    if (timestamp != null) {
                        outputRegisterHistoryDataInfo.reportedDateTime = timestamp;
                    }
                }
            });

            data.add(outputRegisterHistoryDataInfo);
        });
        return data;
    }

    private Optional<Pair<ReadingModificationFlag, ReadingQuality>> extractModificationFlag(JournaledReadingRecord readingRecord, DataValidationStatus validationStatus) {
        return Optional.ofNullable(
                ReadingModificationFlag.getModificationFlagWithQualityRecord(
                        validationStatus.getReadingQualities(),
                        Optional.ofNullable(readingRecord)));
    }
}
