package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;

import java.util.Date;
import java.util.List;
import java.util.Map;

class ChannelRuleValidator {

    private final ValidationRuleImpl rule;

    ChannelRuleValidator(ValidationRuleImpl rule) {
        this.rule = rule;
    }

    Date validateReadings(Channel channel, Interval interval) {
        Date lastChecked = null;
        Interval intervalToRequest = interval.withStart(new Date(interval.getStart().getTime() - 1));
        ListMultimap<Date, ReadingQualityRecord> existingReadingQualities = getExistingReadingQualities(channel, intervalToRequest);
        for (ReadingType channelReadingType : channel.getReadingTypes()) {
            if (rule.getReadingTypes().contains(channelReadingType)) {
                Validator validator = newValidator(channel, interval, channelReadingType);

                ReadingQualityType readingQualityType = rule.getReadingQualityType();
                if (channel.isRegular()) {
                    for (IntervalReadingRecord intervalReading : channel.getIntervalReadings(channelReadingType, intervalToRequest)) {
                        ValidationResult result = validator.validate(intervalReading);
                        lastChecked = handleValidationResult(result, channel, lastChecked, existingReadingQualities, readingQualityType, intervalReading);
                    }
                } else {
                    for (ReadingRecord readingRecord : channel.getRegisterReadings(channelReadingType, interval)) {
                        ValidationResult result = validator.validate(readingRecord);
                        lastChecked = handleValidationResult(result, channel, lastChecked, existingReadingQualities, readingQualityType, readingRecord);
                    }
                }
                Map<Date, ValidationResult> finalValidationResults = validator.finish();
                for (Map.Entry<Date, ValidationResult> entry : finalValidationResults.entrySet()) {
                    lastChecked = handleValidationResult(entry.getValue(), channel, lastChecked, existingReadingQualities, readingQualityType, entry.getKey());
                }
            }
        }
        return lastChecked;
    }

    private ListMultimap<Date, ReadingQualityRecord> getExistingReadingQualities(Channel channel, Interval interval) {
        List<ReadingQualityRecord> readingQualities = channel.findReadingQuality(interval);
        return ArrayListMultimap.create(Multimaps.index(readingQualities, new Function<ReadingQualityRecord, Date>() {
            @Override
            public Date apply(ReadingQualityRecord input) {
                return input.getReadingTimestamp();
            }
        }));
    }

    private Validator newValidator(Channel channel, Interval interval, ReadingType channelReadingType) {
        Validator newValidator = rule.createNewValidator();
        newValidator.init(channel, channelReadingType, interval);
        return newValidator;
    }


    private Date handleValidationResult(ValidationResult result, Channel channel, Date lastChecked, ListMultimap<Date, ReadingQualityRecord> existingReadingQualities,
                                        ReadingQualityType readingQualityType, BaseReadingRecord readingRecord) {
        Optional<ReadingQualityRecord> existingQualityForType = getExistingReadingQualitiesForType(existingReadingQualities, readingQualityType, readingRecord.getTimeStamp());
        if (ValidationResult.SUSPECT.equals(result)) {
            if (!existingQualityForType.isPresent()) {
                ReadingQualityRecord readingQualityRecord = saveNewReadingQuality(channel, readingRecord, readingQualityType);
                existingReadingQualities.put(readingRecord.getTimeStamp(), readingQualityRecord);
                readingRecord.setProcessingFlags(ProcessStatus.Flag.SUSPECT);
            } else if (!existingQualityForType.get().isActual()) {
                existingQualityForType.get().makeActual();
            }
            java.util.Optional<ReadingQualityRecord> suspectQuality = existingReadingQualities.get(readingRecord.getTimeStamp()).stream()
                    .filter(ReadingQualityRecord::isSuspect)
                    .findFirst();
            ReadingQualityRecord suspectQualityRecord = suspectQuality.orElseGet(() -> {
                ReadingQualityRecord record = saveNewReadingQuality(channel, readingRecord, ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT));
                existingReadingQualities.put(readingRecord.getTimeStamp(), record);
                return record;
            });
            if (!suspectQualityRecord.isActual()) {
                suspectQualityRecord.makeActual();
            }
        }
        if (ValidationResult.VALID.equals(result) && existingQualityForType.isPresent() && existingQualityForType.get().isActual()) {
            existingQualityForType.get().makePast();
        }
        return determineLastChecked(result, lastChecked, readingRecord.getTimeStamp());
    }

    private Date handleValidationResult(ValidationResult result, Channel channel, Date lastChecked, ListMultimap<Date, ReadingQualityRecord> existingReadingQualities,
                                        ReadingQualityType readingQualityType, Date timestamp) {
        Optional<ReadingQualityRecord> existingQualityForType = getExistingReadingQualitiesForType(existingReadingQualities, readingQualityType, timestamp);
        if (ValidationResult.SUSPECT.equals(result) && !existingQualityForType.isPresent()) {
            ReadingQualityRecord readingQualityRecord = saveNewReadingQuality(channel, timestamp, readingQualityType);
            existingReadingQualities.put(timestamp, readingQualityRecord);
        }
        if (ValidationResult.VALID.equals(result) && existingQualityForType.isPresent()) {
            existingQualityForType.get().delete();
            existingReadingQualities.remove(timestamp, existingQualityForType);
        }
        return determineLastChecked(result, lastChecked, timestamp);
    }

    private Optional<ReadingQualityRecord> getExistingReadingQualitiesForType(ListMultimap<Date, ReadingQualityRecord> existingReadingQualities, final ReadingQualityType readingQualityType, Date timeStamp) {
        List<ReadingQualityRecord> iterable = existingReadingQualities.get(timeStamp);
        return iterable == null ? Optional.<ReadingQualityRecord>absent() : Iterables.tryFind(iterable, new Predicate<ReadingQualityRecord>() {
            @Override
            public boolean apply(ReadingQualityRecord input) {
                return readingQualityType.equals(input.getType());
            }
        });
    }

    private Date determineLastChecked(ValidationResult result, Date lastChecked, Date timestamp) {
        Date newLastChecked = lastChecked;
        if (!ValidationResult.NOT_VALIDATED.equals(result)) {
            newLastChecked = lastChecked == null ? timestamp : Ordering.natural().max(lastChecked, timestamp);
        }
        return newLastChecked;
    }

    private ReadingQualityRecord saveNewReadingQuality(Channel channel, BaseReadingRecord reading, ReadingQualityType readingQualityType) {
        ReadingQualityRecord readingQuality = channel.createReadingQuality(readingQualityType, reading);
        readingQuality.save();
        return readingQuality;
    }

    private ReadingQualityRecord saveNewReadingQuality(Channel channel, Date timestamp, ReadingQualityType readingQualityType) {
        ReadingQualityRecord readingQuality = channel.createReadingQuality(readingQualityType, timestamp);
        readingQuality.save();
        return readingQuality;
    }


}
